package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.configs.Config._
import ru.juliomoralez.reader.{PaymentFileNameFromConf, PaymentFileNameFromHttp}

class PaymentsReader extends FlinkStreamlet with Serializable {
  @transient val out: AvroOutlet[Message] = AvroOutlet[Message]("out")
  @transient val shape: StreamletShape = StreamletShape.withOutlets(out)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic: FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val paymentFromHttp = context.streamletConfig.getBoolean(paymentFromHttpConf.key)
        val reader = if (paymentFromHttp) {
          new PaymentFileNameFromHttp
        } else {
          new PaymentFileNameFromConf
        }
        val source = reader.readPayment(context)
        writeStream(out, source)
      } catch {
        case e: Exception =>
          log.error("PaymentParticipant error", e)
          throw e
      }
    }
  }
}
