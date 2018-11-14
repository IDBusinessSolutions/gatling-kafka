package io.gatling.mnogu.gatling.kafka.protocol

import akka.actor.ActorRef
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class KafkaComponents(kafkaProtocol: KafkaProtocol,  tracker : ActorRef) extends ProtocolComponents {

  override def onStart: Option[Session => Session] = None

  override def onExit: Option[Session => Unit] = None

}
