package io.gatling.mnogu.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.protocol.KafkaCheck

case class KafkaAttributes[K, V](requestName: Expression[String],
                                 key: Option[Expression[K]],
                                 payload: Expression[V],
                                 checks:            List[KafkaCheck]                           = Nil)
