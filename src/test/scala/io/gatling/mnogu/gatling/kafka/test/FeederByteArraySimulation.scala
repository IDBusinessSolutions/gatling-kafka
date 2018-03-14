package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.mnogu.gatling.kafka.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class FeederByteArraySimulation extends Simulation {
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
    .feed(csv("test.csv").circular)
    .exec(
      kafka("request")
        .send(session => session("foo").validate[String].map(s => s.getBytes)))

  setUp(
    scn
      .inject(constantUsersPerSec(10) during(90 seconds)))
    .protocols(kafkaConf)
}
