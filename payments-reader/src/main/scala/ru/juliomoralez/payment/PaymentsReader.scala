package ru.juliomoralez.payment

import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.Message
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import ru.juliomoralez.config.Config.{config, fileDir, fileRegex}

import scala.jdk.CollectionConverters.asScalaBufferConverter

object PaymentsReader extends FlinkStreamlet {
  val out: AvroOutlet[Message] = AvroOutlet[Message]("out")
  val shape: StreamletShape = StreamletShape.withOutlets(out)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic: FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        def getFiles(fileDir: String, fileRegex: String): Vector[Path] = {
          Files
            .list(Paths.get(fileDir))
            .filter(_.getFileName.toString.matches(fileRegex))
            .collect(Collectors.toList[Path])
            .asScala
            .toVector
        }
        val env: StreamExecutionEnvironment = context.env
        env.setParallelism(1)
        val paths: Vector[Path] = getFiles(
          context.streamletConfig.getString(fileDir.key),
          context.streamletConfig.getString(fileRegex.key)
        )
        paths.foreach(path => {
          writeStream(out, env.readTextFile(path.getFileName.toString).map(message => {
            log.info(message)
            Message(message)
          }))
        })
      } catch {
        case e: Exception => log.error(e.getStackTrace.mkString)
      }
    }
  }
}
