package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.{LogLevel, LogMessage, Message, Payment}
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.PaymentChecker.checkTransaction
import ru.juliomoralez.configs.Config.{config, paymentRegexConf}

import scala.util.matching.Regex

class PaymentChecker extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Message]          = AvroInlet[Message]("in")
  @transient val outValid: AvroOutlet[Payment]   = AvroOutlet[Payment]("out-valid")
  @transient val outInvalid: AvroOutlet[LogMessage] = AvroOutlet[LogMessage]("out-invalid")
  @transient val shape: StreamletShape           = StreamletShape(in).withOutlets(outValid, outInvalid)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val paymentRegex = context.streamletConfig.getString(paymentRegexConf.key).r
        val checkedTransaction = readStream(in).map(checkTransaction(_, paymentRegex))

        writeStream(outValid, checkedTransaction.flatMap(_.right.toSeq))
        writeStream(outInvalid, checkedTransaction.flatMap(_.left.toSeq))
      } catch {
        case e: Exception =>
          log.error("PaymentChecker error", e)
          throw e
      }
    }
  }
}

object PaymentChecker extends Serializable {

  def checkTransaction(message: Message, paymentRegex: Regex): Either[LogMessage, Payment] = {
    message.text match {
      case paymentRegex(from, _, to, _, value) => Right(Payment(0, from, to, value.toInt))
      case _                                   => Left(LogMessage(LogLevel.WARNING, s"[${message.text}] incorrect message"))
    }
  }
}
