package io.gatling.mnogu.gatling.kafka.check

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */

sealed trait KafkaCheckScope

object KafkaCheckScope {
  
  case object Topic extends KafkaCheckScope
  case object Offset extends KafkaCheckScope
  case object Body extends KafkaCheckScope
  case object Key extends KafkaCheckScope
}
