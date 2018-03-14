package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.mnogu.gatling.kafka.Predef._
import io.gatling.mnogu.gatling.kafka.protocol.KafkaProtocol
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  //localhost:9092
  val host  = Option(System getProperty "host") getOrElse "localhost"
  val port =  Option(System.getProperty("port")) getOrElse 9092

  val kafkaConf: KafkaProtocol = kafka
    .producerTopic("sanitycheck.t")
    .consumerTopic("sanitycheck.t")
    .matchByCorrelationID
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> s"$host:$port",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.GROUP_ID_CONFIG -> "test-consumer-group"
      )
    )

  val scn: ScenarioBuilder = scenario("Kafka Test")
    .exec(
      kafka("request")
        //Creating an anonymouns function taking Gatling session and returning string as body
        .send[String, String](StringBody( session => s"""{${randomString(10)}}"""), StringBody( session => s"""{${randomString(100)}}""")))

  setUp(
    scn
      .inject(constantUsersPerSec(5) during (5 seconds))).maxDuration(30 seconds)
    .protocols(kafkaConf)

  def randomString(l: Int): String = {
    scala.util.Random.alphanumeric.take(l).mkString
  }
}
