package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.mnogu.gatling.kafka.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class ByteArraySimulation extends Simulation {
  val kafkaConf = kafka
    .producerTopic("test")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.ByteArraySerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.ByteArraySerializer"))

  val scn = scenario("Kafka Test")
    .exec(kafka("request").produceconsume("foo".getBytes: Array[Byte]))

  setUp(
    scn
      .inject(constantUsersPerSec(10) during(90 seconds)))
    .protocols(kafkaConf)
}
