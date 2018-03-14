package io.gatling.mnogu.gatling.kafka.protocol

import io.gatling.core.action.Action
import io.gatling.core.session.Session

case class KafkaMessage(correlationID: String,
                        matchId:              String,
                        sent:                 Long,
                        checks:               List[KafkaCheck],
                        session:              Session,
                        next:                 Action,
                        title:                String
                      ) {

  def setKafkaCorrelationID(correlationID: String): KafkaMessage = copy(correlationID = correlationID)

}
