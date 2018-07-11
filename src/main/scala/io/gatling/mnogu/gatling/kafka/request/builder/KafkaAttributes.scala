package io.gatling.mnogu.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.protocol.KafkaCheck

case class KafkaAttributes[K, V](requestName: String,
                                 key: Option[Expression[K]],
                                 payload: Expression[V],
                                 testId: String,
                                 checks:            List[KafkaCheck] = Nil)
