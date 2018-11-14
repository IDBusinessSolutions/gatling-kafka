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

import scala.util.control.NonFatal
import scala.util.parsing.json.JSON

/**
  * Define trait for message matching logic with separate request/response checking.
  */
trait KafkaMessageMatcher[M] {

  def reqMatchId(msg: M): Option[String]

  def resMatchId(msg: M): Option[String]

}

/**
  * An implementation of KafkaMessageMatcher based on JSON fields matching.
  * @param reqFieldPath JSON keys sequence to get a matching request message field
  * @param resFieldPath JSON keys sequence to get a matching response message field
  */
class JsonFieldMatcher(reqFieldPath: Seq[String], resFieldPath: Seq[String]) extends KafkaMessageMatcher[String] {

  def getJsonField(msg: String, fieldPath: Seq[String]): Option[String] = {
    try {
      var result = msg
      fieldPath.foreach(path => {
        val json = JSON.parseFull(result)
        json match {
          case Some(map: Map[String, Any]) => result = map(path).asInstanceOf[String]
          case _ => throw new IllegalArgumentException(s"Can't parse message $msg")
        }
      })
      Option.apply(result)
    } catch {
      case NonFatal(e) =>
        Option.empty
    }
  }

  override def reqMatchId(msg: String): Option[String] = getJsonField(msg, reqFieldPath)

  override def resMatchId(msg: String): Option[String] = getJsonField(msg, resFieldPath)
}