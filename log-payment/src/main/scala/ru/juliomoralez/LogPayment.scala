package ru.juliomoralez

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.localrunner.LocalRunner.log
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import juliomoralez.data.LogMessage
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.LogPayment.processLog

class LogPayment extends FlinkStreamlet {
  @transient val inMessage: AvroInlet[LogMessage] = AvroInlet[LogMessage]("in-message")
  @transient val inPayment: AvroInlet[LogMessage] = AvroInlet[LogMessage]("in-payment")
  @transient val shape: StreamletShape  = StreamletShape.withInlets(inMessage, inPayment)

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        processLog(readStream(inMessage))
        processLog(readStream(inPayment))
      } catch {
        case e: Exception =>
          log.error("LogIncorrectPayment error", e)
          throw e
      }
    }
  }
}

object LogPayment extends Serializable {

  def processLog(in: DataStream[LogMessage]): Unit = {
    in.keyBy(0).map(new LogRichFunction)
  }

  class LogRichFunction() extends RichMapFunction[LogMessage, Unit] {
    @transient lazy val countId: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("count-log-message", classOf[Long]))

    override def map(logMessage: LogMessage): Unit = {
      if (countId == null)
        countId.update(1)
      else
        countId.update(countId.value + 1)
      log.info(s"[${logMessage.level.name}][id ${countId.value}] ${logMessage.text}")
    }
  }
}
