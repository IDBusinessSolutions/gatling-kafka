
import io.gatling.sbt.GatlingPlugin

name := "gatling-kafka"
organization := "com.github.mnogu"
version := "0.1.3"
scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-test-framework" % "2.3.0" % "test",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0",
  "io.gatling" % "gatling-core" % "2.3.0",
  ("org.apache.kafka" % "kafka-clients" % "1.0.0")
  // Gatling contains slf4j-api
  .exclude("org.slf4j", "slf4j-api")
)

enablePlugins(GatlingPlugin)


assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}


publishTo <<= version { v: String =>
  val nexus = "https://clds-nexus.eworkbookcloud.com/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "repository/maven-snapshots")
  else
    Some("releases" at nexus + "repository/maven-releases")
}

// From .credentials file
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

// Inline credentials
// credentials += Credentials("Sonatype Nexus Repository Manager", "clds-nexus.eworkbookcloud.com", "ADMIN_USERNAME", "ADMIN_PASSWORD")