package ru.juliomoralez.reader

import java.nio.file.{Files, Path, Paths}

import cloudflow.flink.FlinkStreamletContext
import cloudflow.localrunner.LocalRunner.log
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.configs.Config.{fileDirConf, fileRegexConf}

import scala.util.Try

class PaymentFileNameFromConf extends ReaderFactory {
  def getFile(fileDir: String, fileRegex: String): Option[Path] = {
    Try(Files
      .list(Paths.get(fileDir))
      .filter(_.getFileName.toString.matches(fileRegex))
      .findFirst().get()).toOption
  }

  override def readPayment(context: FlinkStreamletContext): DataStream[Message] = {
    val env = context.env
    env.setParallelism(1)

    val fileDir = context.streamletConfig.getString(fileDirConf.key)
    val fileRegex = context.streamletConfig.getString(fileRegexConf.key)

    getFile(fileDir, fileRegex) match {
      case Some(file) =>
        log.info(s"Read from file ${file.getFileName}")
        env
          .readTextFile(file.getFileName.toString)
          .map(message => {
            log.info(message)
            Message(message)
          })
      case None =>
        log.info("Payment file not found")
        env.fromElements()
    }
  }
}
