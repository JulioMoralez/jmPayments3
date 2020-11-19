package ru.juliomoralez.payment

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import juliomoralez.data.{Message, Payment}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.config.Config.{config, paymentRegex}

import scala.util.matching.Regex

object PaymentChecker extends FlinkStreamlet {
  val in: AvroInlet[Message] = AvroInlet[Message]("in")
  val outValid: AvroOutlet[Payment] = AvroOutlet[Payment]("out-valid")
  val outInvalid: AvroOutlet[Message] = AvroOutlet[Message]("out-invalid")
  val shape: StreamletShape = StreamletShape(in).withOutlets(outValid, outInvalid)

  override def configParameters: Vector[ConfigParameter] = config

  final case class CheckedTransaction(
                     valid: Option[Payment],
                     invalid: Option[Message])

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val paymentR: Regex = context.streamletConfig.getString(paymentRegex.key).r
        def checkTransaction(message: Message): CheckedTransaction = {
          message.text match {
            case paymentR(from, _, to, _, value) => CheckedTransaction(Some(Payment(from, to, value.toInt)), None)
            case _ => CheckedTransaction(None, Some(message))
          }
        }

        val checkedTransaction: DataStream[CheckedTransaction] = readStream(in).map(message => checkTransaction(message))
        writeStream(outValid, checkedTransaction.flatMap(_.valid))
        writeStream(outInvalid, checkedTransaction.flatMap(_.invalid))
      } catch {
        case e: Exception => log.error(e.getStackTrace.mkString)
      }
    }
  }
}
