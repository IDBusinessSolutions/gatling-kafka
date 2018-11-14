/**
  * Copyright 2011-2017 GatlingCorp (http://gatling.io)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package io.gatling.mnogu.gatling.kafka.protocol

import java.lang.{Boolean => JBoolean}
import java.util.{Collections => JCollections, LinkedHashMap => JLinkedHashMap, Map => JMap}

import akka.actor.Props
import io.gatling.commons.stats.{KO, OK, Status}
import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.Failure
import io.gatling.core.action.Action
import io.gatling.core.akka.BaseActor
import io.gatling.core.check.Check
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.mnogu.gatling.kafka.check.KafkaCheck

import scala.collection.mutable


/**
  * Advise actor a message was sent to Kafka Broker
  */
case class MessageSent(
                        matchId: String,
                        sent: Long,
                        checks: List[KafkaCheck],
                        session: Session,
                        next: Action,
                        title: String
                      )

/**
  * Advise actor a response message was received from Kafka Broker
  */
case class MessageReceived(
                            matchId: String,
                            received: Long,
                            message: KafkaMessage
                          )

/**
  * Advise actor that the blocking receive failed
  */
case object BlockingReceiveReturnedNull

case class MessageKey(matchId: String)

object KafkaRequestTrackerActor {
  def props(statsEngine: StatsEngine, configuration: GatlingConfiguration) = Props(new KafkaRequestTrackerActor(statsEngine, configuration))
}


/**
  * Bookkeeping actor to correlate request and response Kafka messages
  * Once a message is correlated, it publishes to the Gatling core DataWriter
  */
class KafkaRequestTrackerActor(statsEngine: StatsEngine, configuration: GatlingConfiguration) extends BaseActor {

  private val sentMessages = mutable.HashMap.empty[MessageKey, MessageSent]
  private val receivedMessages = mutable.HashMap.empty[MessageKey, MessageReceived]
  private val duplicateMessageProtectionEnabled = configuration.jms.acknowledgedMessagesBufferSize > 0
  private val acknowledgedMessagesHistory: JMap[MessageKey, JBoolean] =
    if (duplicateMessageProtectionEnabled)
      new JLinkedHashMap[MessageKey, JBoolean](configuration.jms.acknowledgedMessagesBufferSize, 0.75f, false)
    else
      JCollections.emptyMap()

  // Actor receive loop
  def receive = {

    // message was sent; add the timestamps to the map
    case messageSent@MessageSent(matchId, sent, checks, session, next, title) =>
      //println(s"sent message ${matchId}")
      val messageKey = MessageKey(matchId)
      receivedMessages.get(messageKey) match {
        case None =>
          // normal path
          sentMessages += messageKey -> messageSent
          logger.info(s"[Producer] : Storing message with matchId=$matchId in sentMessages")

        case Some(MessageReceived(_, received, message)) =>
          // message was received out of order, lets just deal with it
          processMessage(session, sent, received, checks, message, next, title)
          receivedMessages -= messageKey
      }

    // message was received; publish to the DataWriter and remove from the map
    case messageReceived@MessageReceived(matchId, received, message) =>
      val messageKey = MessageKey(matchId)
      logger.info(s"[Consumer] : Got a message with matchId=$matchId")

      sentMessages.get(messageKey) match {
        case Some(MessageSent(_, sent, checks, session, next, title)) =>
          // normal path
          logger.debug(s"[Consumer] : Found message with matchId=$matchId in sentMessages")
          processMessage(session, sent, received, checks, message, next, title)
          sentMessages -= messageKey
          if (duplicateMessageProtectionEnabled) {
            acknowledgedMessagesHistory.put(messageKey, JBoolean.TRUE)
          }

        case None =>

          if (!acknowledgedMessagesHistory.containsValue(messageKey)) {
            // failed to find message; early receive? or bad return correlation id?
            // let's add it to the received messages buffer just in case
            receivedMessages += messageKey -> messageReceived
          }

      }

    case BlockingReceiveReturnedNull =>
      //Fail all the sent messages because we do not even have a correlation id
      sentMessages.foreach {
        case (messageKey, MessageSent( _, sent, _, session, next, title)) =>
          executeNext(session, sent, nowMillis, KO, next, title, Some("Blocking received returned null"))
          sentMessages -= messageKey
      }
  }

  private def executeNext(
                           session: Session,
                           sent: Long,
                           received: Long,
                           status: Status,
                           next: Action,
                           title: String,
                           message: Option[String] = None
                         ) = {

    val timings = ResponseTimings(sent, received)

    //TODO I was not able to call session.logGroupResponse from here with the package name as
    //TODO *com.github*.mnogu.gatling.kafka.protocol, therefore for now I've renamed it to *io.gatling*.mnogu
    statsEngine.logResponse(session, title, timings, status, None, message)
    next ! session.logGroupRequest(timings.responseTime, status).increaseDrift(nowMillis - received)
  }

  /**
    * Processes a matched message
    */
  private def processMessage(
                              session: Session,
                              sent: Long,
                              received: Long,
                              checks: List[KafkaCheck],
                              message: KafkaMessage,
                              next: Action,
                              title: String
                            ): Unit = {
    // run all the checks, advise the Gatling API that it is complete and move to next
    val (checkSaveUpdate, error) = Check.check(message, session, checks)
    val newSession = checkSaveUpdate(session)
    error match {
      case None => executeNext(newSession, sent, received, OK, next, title)
      case Some(Failure(errorMessage)) => executeNext(newSession.markAsFailed, sent, received, KO, next, title, Some(errorMessage))
    }
  }
}
