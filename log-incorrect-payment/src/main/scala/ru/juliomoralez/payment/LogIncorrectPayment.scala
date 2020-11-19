package ru.juliomoralez.payment

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation

object LogIncorrectPayment extends FlinkStreamlet{
  val in: AvroInlet[Message] = AvroInlet[Message]("in")
  val shape: StreamletShape = StreamletShape(in)

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        readStream(in).map(x => log.info(x.text))
      } catch {
        case e: Exception =>
          log.error("LogIncorrectPayment error", e)
          throw e
      }
    }
  }
}
