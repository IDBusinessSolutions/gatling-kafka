package io.gatling.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.mnogu.gatling.kafka.Predef._
import io.gatling.mnogu.gatling.kafka.protocol.KafkaProtocol
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._

class Producer extends Simulation {

  //localhost:9092
  val host  = Option(System getProperty "host") getOrElse "localhost"
  val port =  Option(System.getProperty("port")) getOrElse 9092

  val kafkaConf: KafkaProtocol = kafka
    .producerTopic("my-topic1")
    .matchByCorrelationID
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> s"$host:$port",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
      )
    )

  val scn: ScenarioBuilder = scenario("Kafka Test")
    .exec(
      kafka("request")
        //Creating an anonymouns function taking Gatling session and returning string as body
        //.produce[String](StringBody( session => s"""{${randomString(100)}}""")))
        .produce[String](StringBody("AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDDEEEEEEEEEE")))
  //.send[String](( StringBody( session => s"""{${randomString(100)}}"""))))

  setUp(
    scn
      .inject(constantUsersPerSec(2000000) during (15 seconds))).maxDuration(30 seconds)
    .protocols(kafkaConf)

  def randomString(l: Int): String = {
    scala.util.Random.alphanumeric.take(l).mkString
  }
}
