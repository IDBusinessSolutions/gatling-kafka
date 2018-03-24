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

  val host  = Option(System getProperty "host") getOrElse "localhost"
  val port =  Option(System.getProperty("port")) getOrElse 9092
  val acks =  Option(System.getProperty("acks")) getOrElse "1"
  val consumergroup =  Option(System.getProperty("consumergroup")) getOrElse "gatling-consumer-group"
  val producertopic =  Option(System.getProperty("producertopic")) getOrElse "sanitycheck.t"
  val consumertopic =  Option(System.getProperty("consumertopic")) getOrElse "sanitycheck.t"

  val kafkaConf: KafkaProtocol = kafka
    .producerTopic(producertopic)
    .consumerTopic(consumertopic)
    .matchByCorrelationID
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> acks,
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> s"$host:$port",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
        ConsumerConfig.GROUP_ID_CONFIG -> consumergroup
      )
    )

  val scn: ScenarioBuilder = scenario("Kafka Test")
    .exec(
      kafka("Kafka Request Reply Test")
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
