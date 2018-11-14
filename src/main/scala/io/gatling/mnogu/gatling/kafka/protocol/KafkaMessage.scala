package io.gatling.mnogu.gatling.kafka.protocol

case class KafkaMessage(
                         key: String,
                         value: String,
                         topic: String
                       ) {}
