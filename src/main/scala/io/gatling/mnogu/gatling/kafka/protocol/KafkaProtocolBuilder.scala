package io.gatling.mnogu.gatling.kafka.protocol

import io.gatling.core.config.GatlingConfiguration

object KafkaProtocolBuilder {

  implicit def toKafkaProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  def apply(configuration: GatlingConfiguration): KafkaProtocolBuilder =
    KafkaProtocolBuilder(KafkaProtocol(configuration))

}


case class KafkaProtocolBuilder(kafkaProtocol: KafkaProtocol, messageMatcher: KakfaMessageMatcher = CorrelationIDMessageMatcher) {

  def matchByCorrelationID: KakfaMessageMatcher = messageMatcher

  def build: KafkaProtocol = kafkaProtocol

}