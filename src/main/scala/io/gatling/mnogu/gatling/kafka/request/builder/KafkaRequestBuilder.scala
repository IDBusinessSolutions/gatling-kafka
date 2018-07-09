package io.gatling.mnogu.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.action.{KafkaRequestActionBuilder, KafkaRequestResponseActionBuilder}

case class KafkaRequestBuilder(requestName: Expression[String]) {

  def send[V](payload: Expression[V]): KafkaRequestResponseActionBuilder[_, V] = send(payload, None)

  def send[K, V](key: Expression[K], payload: Expression[V]): KafkaRequestResponseActionBuilder[K, V] = send(payload, Some(key))

  private def send[K, V](payload: Expression[V], key: Option[Expression[K]]): KafkaRequestResponseActionBuilder[K, V] =
    new KafkaRequestResponseActionBuilder(KafkaAttributes(requestName, key, payload))

  def produce[V](payload: Expression[V]): KafkaRequestActionBuilder[_, V] = produce(payload, None)

  def produce[K, V](key: Expression[K], payload: Expression[V]): KafkaRequestActionBuilder[K, V] = produce(payload, Some(key))

  private def produce[K, V](payload: Expression[V], key: Option[Expression[K]]): KafkaRequestActionBuilder[K, V] =
    new KafkaRequestActionBuilder(KafkaAttributes(requestName, key, payload))


}
