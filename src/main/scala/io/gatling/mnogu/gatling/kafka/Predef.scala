package io.gatling.mnogu.gatling.kafka

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.protocol.KafkaProtocolBuilder
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaRequestBuilder

object Predef {

  def kafka(implicit configuration: GatlingConfiguration) = KafkaProtocolBuilder(configuration)

  def kafka(requestName: Expression[String]): KafkaRequestBuilder = KafkaRequestBuilder(requestName)

}
