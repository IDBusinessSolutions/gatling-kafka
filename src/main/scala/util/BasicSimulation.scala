package util

import io.gatling.mnogu.gatling.kafka.Predef._
import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import org.apache.kafka.clients.producer.ProducerConfig


//TODO Get rid of duplicate BasicSimulation class
//I Get the following error when running the KafkaGatlingRunner in the test module
//So I've duplicated the BasicSimulation for now
//Error: Could not find or load main class io.gatling.mnogu.gatling.kafka.test.KafkaGatlingRunner

class BasicSimulation extends Simulation {
  val kafkaConf = kafka
    .producerTopic("mytopic")
    .consumerTopic("mytopic")
    .matchByCorrelationID
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer",
        "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
        "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
        "group.id" -> "test-consumer-group"
      )
    )

  val scn = scenario("Kafka Test")
    .exec(
      kafka("request")
        // message to send
        //Creating an anonymouns fuction taking Gatling session and returning string as body
        .send[String, String](StringBody( session => s"""{${randomString(10)}}"""), StringBody( session => s"""{${randomString(100)}}""")))

  setUp(
    scn
      .inject(atOnceUsers(20)))
      .protocols(kafkaConf)
  // .inject(constantUsersPerSec(5) during (2 seconds)))

  def randomString(l: Int) = {
    scala.util.Random.alphanumeric.take(l).mkString
  }
}
