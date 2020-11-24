package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.PaymentsReader.getPaymentReader
import ru.juliomoralez.configs.Config._
import ru.juliomoralez.reader.{PaymentFileNameFromConf, PaymentFileNameFromHttp, ReaderFactory}

class PaymentsReader extends FlinkStreamlet with Serializable {
  @transient val out: AvroOutlet[Message] = AvroOutlet[Message]("out")
  @transient val shape: StreamletShape = StreamletShape.withOutlets(out)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic: FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val sourcePayment = context.streamletConfig.getString(sourcePaymentConf.key)
        val reader = getPaymentReader(sourcePayment)
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

object PaymentsReader extends Serializable {
  def getPaymentReader(sourcePayment: String): ReaderFactory = {
    sourcePayment match {
      case "http" => new PaymentFileNameFromHttp
      case "file" => new PaymentFileNameFromConf
      case _ => throw new Exception("Source payment type not selected. Parameter in config file 'source-payment' get 'http' or 'file' values")
      }
  }
}
