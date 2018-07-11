package io.gatling.mnogu.gatling.kafka

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.protocol.KafkaProtocolBuilder
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaTestBuilder

object Predef {

  def kafka(implicit configuration: GatlingConfiguration) = KafkaProtocolBuilder(configuration)

  def kafka(requestName: String): KafkaTestBuilder = KafkaTestBuilder(requestName)

}
