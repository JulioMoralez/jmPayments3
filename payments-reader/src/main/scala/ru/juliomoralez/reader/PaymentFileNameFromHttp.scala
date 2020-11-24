package ru.juliomoralez.reader
import cloudflow.flink.FlinkStreamletContext
import cloudflow.localrunner.LocalRunner.log
import juliomoralez.data.Message
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.source.FileProcessingMode
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.configs.Config.defaultPaymentFileDirConf

class PaymentFileNameFromHttp extends ReaderFactory {
  override def readPayment(context: FlinkStreamletContext): DataStream[Message] = {
    val env = context.env
    env.setParallelism(1)

    val defaultPaymentFileDir = context.streamletConfig.getString(defaultPaymentFileDirConf.key)

    val inputFormat = new TextInputFormat(new Path(defaultPaymentFileDir))
    inputFormat.setCharsetName("UTF-8")
    env
      .readFile(inputFormat, defaultPaymentFileDir, FileProcessingMode.PROCESS_CONTINUOUSLY, 100)
      .map { message =>
        log.info(message)
        Message(message)
      }
  }
}
