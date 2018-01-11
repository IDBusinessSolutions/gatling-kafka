
import io.gatling.sbt.GatlingPlugin


name := "gatling-kafka"

organization := "com.github.mnogu"

version := "0.1.2-SNAPSHOT"

scalaVersion := "2.12.3"



libraryDependencies ++= Seq(

  "io.gatling" % "gatling-test-framework" % "2.3.0" % "test",
/*  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0" % "test",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0",*/

  "io.gatling" % "gatling-core" % "2.2.3",
  ("org.apache.kafka" % "kafka-clients" % "0.11.0.0")
    // Gatling contains slf4j-api
    .exclude("org.slf4j", "slf4j-api")
 // "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" % "test",

)

enablePlugins(GatlingPlugin)


/*
// Gatling contains scala-library
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
*/
