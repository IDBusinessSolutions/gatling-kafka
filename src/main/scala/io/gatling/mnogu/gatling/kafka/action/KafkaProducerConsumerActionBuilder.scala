package io.gatling.mnogu.gatling.kafka.action

import com.softwaremill.quicklens._
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.mnogu.gatling.kafka.check.KafkaCheck
import io.gatling.mnogu.gatling.kafka.protocol.{KafkaComponents, KafkaMessageMatcher, KafkaProtocol}
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaAttributes
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

import scala.collection.JavaConverters._


case class KafkaProducerConsumerActionBuilder[K, V](requestAttributes: KafkaAttributes[K, V]) extends ActionBuilder {

  def messageMatchingStrategy(messageMatcher: KafkaMessageMatcher[V]): KafkaProducerConsumerActionBuilder[K, V] =
    this.modify(_.requestAttributes.messageMatcher).using(_ => messageMatcher)

  def check(checks: KafkaCheck*): KafkaProducerConsumerActionBuilder[K, V] =
    this.modify(_.requestAttributes.checks).using(_ ::: checks.toList)

  override def build(ctx: ScenarioContext, next: Action): Action = {

    import ctx.{coreComponents, protocolComponentsRegistry, system, throttled}

    val kafkaComponents: KafkaComponents = protocolComponentsRegistry.components(KafkaProtocol.KafkaProtocolKey)

    val producer = new KafkaProducer[K, V](kafkaComponents.kafkaProtocol.producerProperties.asJava)
    val consumer = new KafkaConsumer[K, V](kafkaComponents.kafkaProtocol.consumerProperties.asJava)

    system.registerOnTermination(producer.close())

    new KafkaProducerConsumerAction(
      producer,
      consumer,
      requestAttributes,
      coreComponents,
      kafkaComponents,
      throttled,
      next
    )
  }
}