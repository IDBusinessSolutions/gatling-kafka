package io.gatling.mnogu.gatling.kafka.protocol

import akka.actor.ActorSystem
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

object KafkaProtocol {

  def apply(configuration: GatlingConfiguration): KafkaProtocol = KafkaProtocol(
    producerTopic = "",
    consumerTopics = List(),
    consumerThreadCount = 4,
    consumerPollCount = 1000,
    producerProperties = Map(),
    consumerProperties = Map()
  )

  val KafkaProtocolKey: ProtocolKey {
    type Protocol = KafkaProtocol
    type Components = KafkaComponents
  } = new ProtocolKey {

    type Protocol = KafkaProtocol
    type Components = KafkaComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[KafkaProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultProtocolValue(configuration: GatlingConfiguration): KafkaProtocol = KafkaProtocol(configuration)

    def newComponents(system: ActorSystem, coreComponents: CoreComponents): KafkaProtocol => KafkaComponents = {
      val tracker = system.actorOf(KafkaRequestTrackerActor.props(coreComponents.statsEngine, coreComponents.configuration), "kafkaRequestTracker")
      kafkaProtocol => KafkaComponents(kafkaProtocol, tracker)
    }
  }
}

case class KafkaProtocol(
                          producerTopic: String,
                          consumerTopics: List[String],
                          producerProperties: Map[String, Object],
                          consumerProperties: Map[String, Object],
                          consumerThreadCount: Int,
                          consumerPollCount: Int) extends Protocol {
  def producerTopic(producerTopic: String): KafkaProtocol = copy(producerTopic = producerTopic)

  def consumerTopics(consumerTopics: List[String]): KafkaProtocol = copy(consumerTopics = consumerTopics)

  def producerProperties(properties: Map[String, Object]): KafkaProtocol = copy(producerProperties = properties)

  def consumerProperties(properties: Map[String, Object]): KafkaProtocol = copy(consumerProperties = properties)

  def consumerThreadCount(consumerThreadCount: Int): KafkaProtocol = copy(consumerThreadCount = consumerThreadCount)

  def consumerPollCount(consumerPollCount: Int): KafkaProtocol = copy(consumerPollCount = consumerPollCount)
}
