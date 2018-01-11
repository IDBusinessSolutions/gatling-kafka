package com.github.mnogu.gatling.kafka.test

import io.gatling.core.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._
import com.github.mnogu.gatling.kafka.Predef._
import io.gatling.core.structure.ScenarioBuilder




class BasicSimulation extends Simulation {

  val kafkaConf = kafka
    // Kafka topic name
    .topic("test9")
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


def sendToKafka(s: String) = exec(
   kafka("req").send[String](s))


object Events {

  def callCreated(id: Int) = {
    //call generator to generate the case class with that id.
    sendToKafka(id + "-callCreated")
  }

  def callEnded(id: Int) = {

    sendToKafka(id + "-callEnded")
  }

  def accountCreated(id: Int) = {
    sendToKafka(id + "-accountCreated")

  }


  def accountEnded(id: Int) = {
    sendToKafka(id + "-accountEnded")
  }

}

  val r = scala.util.Random


  //write an entity events outcomes
  def callEntity: ScenarioBuilder = {

    val eid = r.nextInt(100)
    scenario("CallEntity")
      //entity Id(which is a User in terms of gatling) must be created each time.
      .exec(Events.callCreated(eid), Events.callEnded(eid))
  }


  def accountEntity: ScenarioBuilder = {
    val eid = r.nextInt(100)
    scenario("AccountEntity")
      .exec(Events.accountCreated(eid), Events.accountEnded(eid))
  }


  setUp(
    callEntity
      .inject(constantUsersPerSec(5) during(10 seconds)),
    accountEntity
        .inject(constantUsersPerSec(5) during(10 seconds))
  )
    .protocols(kafkaConf)
}
