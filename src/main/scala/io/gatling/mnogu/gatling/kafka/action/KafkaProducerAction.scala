package io.gatling.mnogu.gatling.kafka.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.ClockSingleton._
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session._
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import io.gatling.mnogu.gatling.kafka.protocol._
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaAttributes
import org.apache.kafka.clients.producer._


class KafkaProducerAction[K, V](val producer: KafkaProducer[K, V],
                                val kafkaAttributes: KafkaAttributes[K, V],
                                val coreComponents: CoreComponents,
                                val kafkaComponents: KafkaComponents,
                                val throttled: Boolean,
                                val next: Action)
  extends ExitableAction with NameGen {

  import kafkaComponents.{kafkaProtocol, tracker}

  val statsEngine = coreComponents.statsEngine
  statsEngine.start()
  val name = genName("KafkaRequestAction")
  val messageMatcher = kafkaAttributes.messageMatcher

  val BlockingReceiveReturnedNullException = new Exception("Blocking receive returned null. Possibly the consumer was closed.")


  def execute(session: Session): Unit = {

    sendRequest(
      kafkaAttributes.requestName.toString(),
      producer,
      kafkaAttributes,
      throttled,
      session
    )
  }

  private def sendRequest(requestName: String,
                          producer: Producer[K, V],
                          kafkaAttributes: KafkaAttributes[K, V],
                          throttled: Boolean,
                          session: Session
                         ): Validation[Unit] = {

    kafkaAttributes payload session map { payload =>

      val record = kafkaAttributes.key match {
        case Some(k) =>
          new ProducerRecord[K, V](kafkaProtocol.producerTopic, k(session).get, payload)
        case None =>
          new ProducerRecord[K, V](kafkaProtocol.producerTopic, payload)
      }

      val requestStartDate = nowMillis

      producer.send(record, new Callback() {

        override def onCompletion(m: RecordMetadata, e: Exception): Unit = {

          val requestEndDate = nowMillis
          statsEngine.logResponse(
            session,
            requestName,
            ResponseTimings(startTimestamp = requestStartDate, endTimestamp = requestEndDate),
            if (e == null) OK else KO,
            None,
            if (e == null) None else Some(e.getMessage)
          )

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
}
