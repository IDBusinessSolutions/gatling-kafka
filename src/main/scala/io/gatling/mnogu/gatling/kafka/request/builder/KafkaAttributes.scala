package io.gatling.mnogu.gatling.kafka.request.builder

import io.gatling.core.session.Expression
import io.gatling.mnogu.gatling.kafka.check.KafkaCheck
import io.gatling.mnogu.gatling.kafka.protocol.KafkaMessageMatcher

case class KafkaAttributes[K, V](
                                    requestName: String,
                                    key: Option[Expression[K]],
                                    payload: Expression[V],
                                    messageMatcher: KafkaMessageMatcher[V] = null,
                                    checks: List[KafkaCheck] = Nil
                                  )