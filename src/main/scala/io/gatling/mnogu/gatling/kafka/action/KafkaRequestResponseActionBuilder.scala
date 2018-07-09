package io.gatling.mnogu.gatling.kafka.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.mnogu.gatling.kafka.protocol.{KafkaComponents, KafkaProtocol}
import io.gatling.mnogu.gatling.kafka.request.builder.KafkaAttributes
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

import scala.collection.JavaConverters._


class KafkaRequestResponseActionBuilder[K,V](kafkaAttributes: KafkaAttributes[K,V]) extends ActionBuilder {

  override def build( ctx: ScenarioContext, next: Action ): Action = {

    import ctx.{coreComponents, protocolComponentsRegistry, system, throttled}

    val kafkaComponents: KafkaComponents = protocolComponentsRegistry.components(KafkaProtocol.KafkaProtocolKey)

    val producer = new KafkaProducer[K,V]( kafkaComponents.kafkaProtocol.properties.asJava )
    val consumer = new KafkaConsumer[K,V]( kafkaComponents.kafkaProtocol.properties.asJava )

    system.registerOnTermination(producer.close())

    new KafkaRequestResponseAction(
      producer,
      consumer,
      kafkaAttributes,
      coreComponents,
      kafkaComponents,
      throttled,
      next
    )
  }
}