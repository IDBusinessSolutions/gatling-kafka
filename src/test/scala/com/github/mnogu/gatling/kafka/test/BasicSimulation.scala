package com.github.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import org.apache.kafka.clients.producer.ProducerConfig
import scala.concurrent.duration._

import com.github.mnogu.gatling.kafka.Predef._

class Kafka extends Simulation {
  val kafkaConf = kafka
    .topic("mytopic")
    .properties(
    Map(
      ProducerConfig.ACKS_CONFIG -> "1",
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      // in most cases, StringSerializer or ByteArraySerializer
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer",
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
        "org.apache.kafka.common.serialization.StringSerializer"))

  val scn = scenario("Kafka Test")
    .exec(
      kafka("request")
        // message to send
        .send[String](randomString(100)))

  setUp(
    scn
      .inject(constantUsersPerSec(75000) during(90 seconds)))
    .protocols(kafkaConf)

  def randomString(l: Int) = scala.util.Random.alphanumeric.take(l).mkString
}
