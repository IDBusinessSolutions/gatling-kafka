package io.gatling.mnogu.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.action.KafkaRequestActionBuilder

case class KafkaRequestBuilder(requestName: Expression[String]) {

  def send[V](payload: Expression[V]): KafkaRequestActionBuilder[_, V] = send(payload, None)

  def send[K, V](key: Expression[K], payload: Expression[V]): KafkaRequestActionBuilder[K, V] = send(payload, Some(key))

  private def send[K, V](payload: Expression[V], key: Option[Expression[K]]): KafkaRequestActionBuilder[K, V] =
    new KafkaRequestActionBuilder(KafkaAttributes(requestName, key, payload))

}
