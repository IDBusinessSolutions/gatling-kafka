/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.mnogu.gatling.kafka.protocol

import java.util.UUID


/**
 * define trait for message matching logic with separate request/response
 * to see how it can be used check KafkaDefaultMessageMatcher
 */

//TODO Rework how this Matcher Class is used. At the moment messages are correlated using a random matchid kafka header
//generated in @KafkaRequestAction and not the UUID below. Either the strategies provided by this class should be used
//or it should be removed
trait   KakfaMessageMatcher {
  def prepareRequest(msg: KafkaMessage): Unit
  def requestMatchId(msg: KafkaMessage): String
  def responseMatchId(msg: KafkaMessage): String
}


object CorrelationIDMessageMatcher extends KakfaMessageMatcher {
  override def prepareRequest(msg: KafkaMessage): Unit = msg.setKafkaCorrelationID(UUID.randomUUID.toString)
  override def requestMatchId(msg: KafkaMessage): String = msg.correlationID
  override def responseMatchId(msg: KafkaMessage): String = msg.correlationID
}

