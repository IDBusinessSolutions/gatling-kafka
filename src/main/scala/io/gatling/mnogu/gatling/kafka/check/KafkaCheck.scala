package io.gatling.mnogu.gatling.kafka.check

import io.gatling.commons.validation.Validation
import io.gatling.core.check.{Check, CheckResult}
import io.gatling.core.session.Session
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessage

import scala.collection.mutable

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  *
  * This class serves as model for the Kafka-specific checks
  *
  * @param wrapped the underlying check
  */
case class KafkaCheck(wrapped: Check[KafkaMessage], scope: KafkaCheckScope) extends Check[KafkaMessage] {
  override def check(message: KafkaMessage, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] =
    wrapped.check(message, session)
}
