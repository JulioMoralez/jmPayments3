package ru.juliomoralez.reader

import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import cloudflow.flink.FlinkStreamletContext
import cloudflow.localrunner.LocalRunner.log
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import ru.juliomoralez.configs.Config.{fileDir, fileRegex}

import scala.jdk.CollectionConverters.asScalaBufferConverter

class PaymentFileNameFromConf extends ReaderFactory {
  def getFiles(fileDir: String, fileRegex: String): Vector[Path] = {
    Files
      .list(Paths.get(fileDir))
      .filter(_.getFileName.toString.matches(fileRegex))
      .collect(Collectors.toList[Path])
      .asScala
      .toVector
  }

  override def readPayment(context: FlinkStreamletContext): DataStream[Message] = {
    val env = context.env
    env.setParallelism(1)

    val paths = getFiles(
      context.streamletConfig.getString(fileDir.key),
      context.streamletConfig.getString(fileRegex.key)
    )
    if (paths.nonEmpty) {
      log.info(s"Read from file ${paths(0).getFileName}")
      env
        .readTextFile(paths(0).getFileName.toString)
        .map(message => {
          log.info(message)
          Message(message)
        })
    } else {
      log.info("Payment file not found")
      env.fromElements()
    }
  }
}
