package io.gatling.mnogu.gatling.kafka.check

import io.gatling.commons.validation._
import io.gatling.core.check.{Check, Extender, Preparer}
import io.gatling.mnogu.gatling.kafka.check.KafkaCheckScope.Body
import io.gatling.mnogu.gatling.kafka.check.KafkaCheckScope.Topic
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessage

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */
object KafkaCheckBuilders {

  private def extender(target: KafkaCheckScope): Extender[KafkaCheck, KafkaMessage] =
    (wrapped: Check[KafkaMessage]) => KafkaCheck(wrapped, target)

  val TopicExtender = extender(Topic)
  val BodyExtender = extender(Body)

  val PassThroughMessagePreparer: Preparer[KafkaMessage, KafkaMessage] = _.success
}
