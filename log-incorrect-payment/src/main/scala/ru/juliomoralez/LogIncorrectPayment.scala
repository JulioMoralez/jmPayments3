package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation

class LogIncorrectPayment extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Message] = AvroInlet[Message]("in")
  @transient val shape: StreamletShape  = StreamletShape(in)

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
