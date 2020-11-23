package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.{Message, Payment}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.PaymentChecker.checkTransaction
import ru.juliomoralez.configs.Config.{config, paymentRegexConf}

import scala.util.matching.Regex

class PaymentChecker extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Message]          = AvroInlet[Message]("in")
  @transient val outValid: AvroOutlet[Payment]   = AvroOutlet[Payment]("out-valid")
  @transient val outInvalid: AvroOutlet[Message] = AvroOutlet[Message]("out-invalid")
  @transient val shape: StreamletShape           = StreamletShape(in).withOutlets(outValid, outInvalid)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val paymentRegex = context.streamletConfig.getString(paymentRegexConf.key).r
        val checkedTransaction = readStream(in).map(checkTransaction(_, paymentRegex))

        writeStream(outValid, checkedTransaction.flatMap(_.valid))
        writeStream(outInvalid, checkedTransaction.flatMap(_.invalid))
      } catch {
        case e: Exception =>
          log.error("PaymentChecker error", e)
          throw e
      }
    }
  }
}

object PaymentChecker {
  final case class CheckedTransaction(valid: Option[Payment], invalid: Option[Message])

  def checkTransaction(message: Message, paymentRegex: Regex): CheckedTransaction = {
    message.text match {
      case paymentRegex(from, _, to, _, value) => CheckedTransaction(Some(Payment(from, to, value.toInt)), None)
      case _                                   => CheckedTransaction(None, Some(message))
    }
  }
}
