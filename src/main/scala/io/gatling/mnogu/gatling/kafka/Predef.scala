package io.gatling.mnogu.gatling.kafka

import io.gatling.core.config.GatlingConfiguration
import io.gatling.mnogu.gatling.kafka.check.KafkaCheckSupport
import io.gatling.mnogu.gatling.kafka.protocol.KafkaProtocolBuilder
import io.gatling.mnogu.gatling.kafka.request.builder.Kafka

object Predef extends KafkaCheckSupport {

  def kafka(implicit configuration: GatlingConfiguration) = KafkaProtocolBuilder(configuration)

  def kafka(requestName: String) = new Kafka(requestName)

}
