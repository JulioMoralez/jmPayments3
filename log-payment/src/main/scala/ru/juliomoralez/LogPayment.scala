package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.localrunner.LocalRunner.log
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import juliomoralez.data.LogMessage
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.functions.co.RichCoMapFunction
import ru.juliomoralez.LogPayment.LogRichFunction

class LogPayment extends FlinkStreamlet {
  @transient val inMessage: AvroInlet[LogMessage] = AvroInlet[LogMessage]("in-message")
  @transient val inPayment: AvroInlet[LogMessage] = AvroInlet[LogMessage]("in-payment")
  @transient val shape: StreamletShape  = StreamletShape.withInlets(inMessage, inPayment)

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        readStream(inMessage)
          .connect(readStream(inPayment))
          .keyBy(0, 0)
          .map(new LogRichFunction)
      } catch {
        case e: Exception =>
          log.error("LogIncorrectPayment error", e)
          throw e
      }
    }
  }
}

object LogPayment extends Serializable {

  class LogRichFunction() extends RichCoMapFunction[LogMessage, LogMessage, Unit] {
    @transient lazy val countId: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("count-log-message", classOf[Long]))

    override def map1(logMessage: LogMessage): Unit = {
      map(logMessage)
    }

    override def map2(logMessage: LogMessage): Unit = {
      map(logMessage)
    }

    private def map(logMessage: LogMessage): Unit = {
      if (countId == null)
        countId.update(1)
      else
        countId.update(countId.value + 1)
      log.info(s"[${logMessage.level.name}][id ${countId.value}]${logMessage.text}")
    }
  }
}


