package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.mnogu.gatling.kafka.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class ThrottledSimulation extends Simulation {
  val kafkaConf = kafka
    // Kafka topic name
    .producerTopic("test")
    // Kafka producer configs
    .properties(
    Map(
      ProducerConfig.ACKS_CONFIG -> "1",
      // list of Kafka broker hostname and port pairs
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",

      // in most cases, StringSerializer or ByteArraySerializer
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer"))

  val scn = scenario("Kafka Test")
    .forever(
      exec(
        kafka("request")
          // message to send
          .send[String]("foo"))
    )

  setUp(
    scn.inject(atOnceUsers(10)))
    .throttle(jumpToRps(10), holdFor(30 seconds))
    .protocols(kafkaConf)
}
