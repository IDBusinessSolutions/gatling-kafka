package util

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

/**
 * This object simply provides a `main` method that wraps
 * [[io.gatling.app.Gatling]].main, which
 * allows us to do some configuration and setup before
 * Gatling launches.
 */
object KafkaGatlingRunner {

  def main(args: Array[String]) {


    // This sets the class for the simulation we want to run.
    val simClass = classOf[BasicSimulation].getName

    val props = new GatlingPropertiesBuilder
    props.sourcesDirectory("./src/main/scala")
    props.binariesDirectory("./target/scala-2.12/classes")
    props.simulationClass(simClass)
    //props.runDescription(config.runDescription)
    //props.outputDirectoryBaseName(config.simulationId)
    Gatling.fromMap(props.build)

  }
}