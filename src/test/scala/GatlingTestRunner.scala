import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.mnogu.gatling.kafka.test.BasicSimulation

/**
  * Created by Roman_Rybalkin 
  * 11/13/2018
  */
object GatlingTestRunner {

  def main(args: Array[String]) {
    val simClass = classOf[BasicSimulation].getName

    val props = new GatlingPropertiesBuilder
    props.sourcesDirectory("./src/main/scala")
    props.binariesDirectory("./target/scala-2.12/classes")
    props.simulationClass(simClass)

    Gatling.fromMap(props.build)
  }
}
