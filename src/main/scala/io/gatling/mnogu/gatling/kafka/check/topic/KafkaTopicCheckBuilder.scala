package io.gatling.mnogu.gatling.kafka.check.topic

import io.gatling.commons.validation._
import io.gatling.core.session._
import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.core.check.extractor.{Extractor, SingleArity}
import io.gatling.mnogu.gatling.kafka.check.KafkaCheck
import io.gatling.mnogu.gatling.kafka.check.KafkaCheckBuilders._
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessage

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */
object KafkaTopicCheckBuilder {

  val TopicExtractor = new Extractor[KafkaMessage, String] with SingleArity {
    val name = "topic"
    def apply(prepared: KafkaMessage) = Option.apply(prepared).map(_.topic) match {
      case topic => topic.success
      case _ => "Message wasn't received".failure
    }
  }.expressionSuccess

  val Topic = new DefaultFindCheckBuilder[KafkaCheck, KafkaMessage, KafkaMessage, String](
    TopicExtender,
    PassThroughMessagePreparer,
    TopicExtractor
  )
}
