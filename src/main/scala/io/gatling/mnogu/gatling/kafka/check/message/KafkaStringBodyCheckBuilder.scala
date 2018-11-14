package io.gatling.mnogu.gatling.kafka.check.message

import io.gatling.commons.validation._
import io.gatling.core.session._
import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.core.check.extractor.{Extractor, SingleArity}
import io.gatling.mnogu.gatling.kafka.check.KafkaCheck
import io.gatling.mnogu.gatling.kafka.check.KafkaCheckBuilders.{BodyExtender, PassThroughMessagePreparer}
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessage

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */
object KafkaStringBodyCheckBuilder {

  val BodyExtractor = new Extractor[KafkaMessage, String] with SingleArity {
    val name = "body"
    def apply(prepared: KafkaMessage) = Option.apply(prepared).map(_.value) match {
      case value => value.success
      case _ => "Message wasn't received".failure
    }
  }.expressionSuccess

  val Body = new DefaultFindCheckBuilder[KafkaCheck, KafkaMessage, KafkaMessage, String](
    BodyExtender,
    PassThroughMessagePreparer,
    BodyExtractor
  )
}
