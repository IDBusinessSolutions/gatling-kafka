package io.gatling.mnogu.gatling.kafka.protocol

import akka.actor.ActorSystem
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

object KafkaProtocol {

  def apply(configuration: GatlingConfiguration): KafkaProtocol = KafkaProtocol (
    producerTopic = "",
    consumerTopic = "",
    consumerThreadCount = 4,
    properties = Map(),
    messageMatcher = CorrelationIDMessageMatcher
  )

  val KafkaProtocolKey = new ProtocolKey {

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
  consumerTopic: String,
  properties: Map[String, Object],
  consumerThreadCount: Int,
  messageMatcher: KakfaMessageMatcher = CorrelationIDMessageMatcher) extends Protocol {
  def producerTopic(producerTopic: String): KafkaProtocol = copy(producerTopic = producerTopic)
  def consumerTopic(consumerTopic: String): KafkaProtocol = copy(consumerTopic = consumerTopic)
  def properties(properties: Map[String, Object]): KafkaProtocol = copy(properties = properties)
  def consumerThreadCount(consumerThreadCount: Int): KafkaProtocol = copy(consumerThreadCount = consumerThreadCount)
  def matchByCorrelationID: KafkaProtocol = copy(messageMatcher = messageMatcher)
}
