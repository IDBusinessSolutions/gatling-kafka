package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.mnogu.gatling.kafka.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class FeederSimulation extends Simulation {
  val kafkaConf = kafka
    .producerTopic("test")
    .producerProperties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer"))

  val scn = scenario("Kafka Test")
    // You can also use feeder
    .feed(csv("test.csv").circular)
    .exec(kafka("request").produceconsume[String]("${foo}"))

  setUp(
    scn
      .inject(constantUsersPerSec(10) during(90 seconds)))
    .protocols(kafkaConf)
}
