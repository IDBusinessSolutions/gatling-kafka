package io.gatling.mnogu.gatling.kafka.action

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorRef
import io.gatling.commons.util.ClockSingleton._
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session._
import io.gatling.core.util.NameGen
import io.gatling.mnogu.gatling.kafka.protocol._
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaAttributes
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.{Header, Headers}

import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.control.NonFatal


class KafkaRequestAction[K, V](val producer: KafkaProducer[K, V],
                               val consumer: KafkaConsumer[K, V],
                               val kafkaAttributes: KafkaAttributes[K, V],
                               val coreComponents: CoreComponents,
                               val kafkaProtocol: KafkaProtocol,
                               val tracker: ActorRef,
                               val throttled: Boolean,
                               val next: Action)
  extends  ExitableAction with NameGen  {

  val statsEngine = coreComponents.statsEngine
  statsEngine.start()
  val name = genName("kafkaRequest")
  val messageMatcher = kafkaProtocol.messageMatcher

  val BlockingReceiveReturnedNullException = new Exception("Blocking receive returned null. Possibly the consumer was closed.")



  def execute(session: Session): Unit = {

      val matchId : String = randomString(10)

        sendRequest(
          kafkaAttributes.requestName.toString(),
          producer,
          kafkaAttributes,
          throttled,
          session,
          matchId
        )

      val startDate = nowMillis
      tracker ! MessageSent(kafkaProtocol.producerTopic, matchId, startDate, kafkaAttributes.checks, session, next, "Send Kafka Message")

  }


    class ListenerThread(val continue: AtomicBoolean = new AtomicBoolean(true)) extends Thread(new Runnable {
      def run(): Unit = {

        val consumer = new KafkaConsumer[K,V]( kafkaProtocol.properties.asJava )

        consumer.subscribe(java.util.Collections.singletonList(kafkaProtocol.consumerTopic))

        try {
          while (continue.get) {
            val records : ConsumerRecords [K, V] = consumer.poll(1000)

            for (record <-records.asScala){
              record match  {
                case rec: ConsumerRecord [K, V] =>
                  val headers: Headers = rec.headers()
                  val matchIdHeader : Header = headers.lastHeader("matchId")

                  val matchId= new String (matchIdHeader.value(), "UTF-8")

                  tracker ! MessageReceived(rec.topic(), matchId, nowMillis, null)
                case _ =>
                  tracker ! BlockingReceiveReturnedNull
                  throw BlockingReceiveReturnedNullException
              }
            }
          }
        } catch {
          // when we close, receive can throw exception
          case NonFatal(e) => logger.error(e.getMessage)
        } finally {
          consumer.close()
        }
      }
    }) {
      def close() = {
        continue.set(false)
        interrupt()
        join(1000)
      }
    }

  val listenerThreads = (1 to 4).map(_ => new ListenerThread)

  listenerThreads.foreach(_.start)

  def postStop(): Unit = {
    listenerThreads.foreach(thread => Try(thread.close()).recover { case NonFatal(e) => logger.warn("Could not shutdown listener thread", e) })
  }




  private def sendRequest(requestName: String,
                          producer: Producer[K, V],
                          kafkaAttributes: KafkaAttributes[K, V],
                          throttled: Boolean,
                          session: Session,
                          matchId: String
                         ): Validation[Unit] = {

    kafkaAttributes payload session map { payload =>

      val header: Header = new RecordHeader("matchId", matchId.getBytes("UTF-8"))
      val headers: util.List[Header] = List(header).asJava

      // partition is null so we let kafka decide it
      // unfortunately no API without partition parameter (downside of Java overloads)
      val partition: Int = 0

      val record = kafkaAttributes.key match {
        case Some(k) =>
          new ProducerRecord[K, V](kafkaProtocol.producerTopic,partition, k(session).get, payload, headers)
        case None =>
          new ProducerRecord[K, V](kafkaProtocol.producerTopic, payload)
      }

      val requestStartDate = nowMillis

      producer.send(record, new Callback() {

        override def onCompletion(m: RecordMetadata, e: Exception): Unit = {

          val requestEndDate = nowMillis


          if (throttled) {
            coreComponents.throttler.throttle(session.scenario, () => next ! session)
          } else {
            next ! session
          }

        }
      })

    }

  }

  def logMessage(text: String): Unit = {
    logger.info(text)
  }

  def randomString(l: Int) = scala.util.Random.alphanumeric.take(l).mkString

}
