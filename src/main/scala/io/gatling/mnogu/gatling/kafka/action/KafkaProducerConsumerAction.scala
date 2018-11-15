package io.gatling.mnogu.gatling.kafka.action

import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

import io.gatling.commons.util.ClockSingleton._
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine
import io.gatling.core.util.NameGen
import io.gatling.mnogu.gatling.kafka.protocol._
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaAttributes
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer._

import scala.collection.JavaConverters._
import scala.util.control.NonFatal


class KafkaProducerConsumerAction[K, V](val producer: KafkaProducer[K, V],
                                        val consumer: KafkaConsumer[K, V],
                                        val kafkaAttributes: KafkaAttributes[K, V],
                                        val coreComponents: CoreComponents,
                                        val kafkaComponents: KafkaComponents,
                                        val throttled: Boolean,
                                        val next: Action)
  extends ExitableAction with NameGen {

  import kafkaAttributes.{messageMatcher, requestName}
  import kafkaComponents.{kafkaProtocol, tracker}

  val statsEngine: StatsEngine = coreComponents.statsEngine
  statsEngine.start()

  val name: String = genName("KafkaProducerConsumerAction")
  val uniqueId: String = UUID.randomUUID().toString

  /** flag to indicate that Kafka consumers started successfully **/
  val listening: AtomicBoolean = new AtomicBoolean(false)

  val BlockingReceiveReturnedNullException = new Exception("Blocking receive returned null. Possibly the consumer was closed.")

  // the main action method
  override def execute(session: Session): Unit = {
    val payload = kafkaAttributes.payload(session).get
    val matchId = messageMatcher.reqMatchId(payload)

    logger.debug(s"Payload = $payload, calculated matchId field = $matchId")
    if (matchId.isEmpty)
      throw new IllegalArgumentException(s"Can't parse matchId field for payload $payload")

    val startDate = nowMillis
    tracker ! MessageSent(matchId.get, startDate, kafkaAttributes.checks, session, next, requestName)

    sendRequest(
      kafkaAttributes.requestName.toString,
      producer,
      kafkaAttributes,
      throttled,
      session,
      payload,
      matchId.get
    )
  }

  private def sendRequest(requestName: String,
                          producer: Producer[K, V],
                          kafkaAttributes: KafkaAttributes[K, V],
                          throttled: Boolean,
                          session: Session,
                          payload: V,
                          matchId: String
                         ) = {

    val record = kafkaAttributes.key match {
      case Some(k) =>
        new ProducerRecord[K, V](kafkaProtocol.producerTopic, null, k(session).get, payload)
      case None =>
        new ProducerRecord[K, V](kafkaProtocol.producerTopic, payload)
    }

    producer.send(record, (_: RecordMetadata, e: Exception) => {
      if (e != null) {
        logger.error("Error while sending a message to Kafka", e)
      } else {
        logger.debug(s"Record (message) with $matchId has been acknowledged by the server")
      }
    })
  }

  class ListenerThread(val continue: AtomicBoolean = new AtomicBoolean(true)) extends Thread(() => {

    var consumerProperties = kafkaProtocol.consumerProperties
    consumerProperties += ConsumerConfig.GROUP_ID_CONFIG -> s"$name-$uniqueId"
    consumerProperties += ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest"

    val consumer = new KafkaConsumer[K, V](consumerProperties.asJava)

    consumer.subscribe(kafkaProtocol.consumerTopics.asJava)

    // call polling to force Kafka consumer join
    consumer.poll(0)

    // set Kafka connectivity flag
    listening.set(true)

    try {
      while (continue.get) {
        val records: ConsumerRecords[K, V] = consumer.poll(kafkaProtocol.consumerPollCount)
        for (record <- records.asScala) {
          record match {
            case rec: ConsumerRecord[K, V] =>
              try {
                val matchId = messageMatcher.resMatchId(rec.value())

                if (matchId.isDefined) {
                  val kafkaMessage = KafkaMessage(
                    Option(record.key()).map(_.toString) getOrElse "",
                    Option(record.value()).map(_.toString) getOrElse "",
                    record.topic()
                  )
                  tracker ! MessageReceived(matchId.get, nowMillis, kafkaMessage)

                } else {
                  // can happen if message is not expected
                  logger.debug(s"The consumed message=$record ignored because can't get matchId")
                }
              } catch {
                // when we close, receive can throw exception
                case NonFatal(e) => logger.error(s"Error while parsing a consumer record: ${e.getMessage}")
              }
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

  ) {
    def close(): Unit = {
      continue.set(false)
      interrupt()
      join(1000)
    }
  }

  // run Kafka listener threads
  (1 to kafkaProtocol.consumerThreadCount).map(_ => new ListenerThread).foreach(t => {
    // use daemon threads to stop when main gatling threads finish
    t.setDaemon(true)
    t.start()
  })

  // waiting until Kafka consumers connected
  while(!listening.get) {
    Thread.sleep(100)
  }

}
