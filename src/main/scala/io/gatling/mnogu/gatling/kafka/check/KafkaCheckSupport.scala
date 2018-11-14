package io.gatling.mnogu.gatling.kafka.check

import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.mnogu.gatling.kafka.check.message.KafkaStringBodyCheckBuilder
import io.gatling.mnogu.gatling.kafka.check.topic.KafkaTopicCheckBuilder
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessage

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */
trait KafkaCheckSupport {

  val topic: DefaultFindCheckBuilder[KafkaCheck, KafkaMessage, KafkaMessage, String] = KafkaTopicCheckBuilder.Topic

  val body: DefaultFindCheckBuilder[KafkaCheck, KafkaMessage, KafkaMessage, String] = KafkaStringBodyCheckBuilder.Body
}
