package ru.juliomoralez.payment

import java.nio.file.{Files, Path, Paths}
import java.util.stream.Collectors

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.Message
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.api.java.io.RowCsvInputFormat
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import ru.juliomoralez.config.Config.{config, fileDir, fileRegex}

import scala.jdk.CollectionConverters.asScalaBufferConverter

object PaymentsReader extends FlinkStreamlet {
  val in: AvroInlet[Message] = AvroInlet[Message]("in")
  val out: AvroOutlet[Message] = AvroOutlet[Message]("out")
  val shape: StreamletShape = StreamletShape(in).withOutlets(out)

  def getFiles(fileDir: String, fileRegex: String): Vector[Path] = {
    Files
      .list(Paths.get(fileDir))
      .filter(_.getFileName.toString.matches(fileRegex))
      .collect(Collectors.toList[Path])
      .asScala
      .toVector
  }

  def readFilesWithRegex: Unit = {

  }

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic: FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val env: StreamExecutionEnvironment = context.env
        env.setParallelism(1)
        // принимаем имя файла в виде {"text":"file1.txt"}
        readStream(in).map(filepath => {
          log.info(s"Выполнить чтение из файла ${filepath.text}")
//          env.readTextFile(filepath.text).map(Message(_))
        })//.map(writeStream(out, _))

        //старая функция с чтением из файлов директории fileDir по маске fileRegex
        readFileWithRegex()
        def readFileWithRegex(): Unit = {
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
        }

        //чтение с помощью csv с использованием FileInputFormat
        readCsvFile("file.csv")
        def readCsvFile(filepath: String): Unit = {
          val csvTypeInfo: Array[TypeInformation[_]] = Array(
            BasicTypeInfo.STRING_TYPE_INFO,
            BasicTypeInfo.STRING_TYPE_INFO,
            BasicTypeInfo.INT_TYPE_INFO)
          import org.apache.flink.core.fs.Path
          val inputFormat: RowCsvInputFormat = new RowCsvInputFormat(new Path(filepath), csvTypeInfo, "\n", ";")
          env.readFile(inputFormat, filepath).map(x => log.info(s"!!!!!!!!!!!!!!!!!!!!!!!! ${x.toString}"))
        }
      } catch {
        case e: Exception =>
          log.error("PaymentsReader error", e)
          throw e
      }
    }
  }
}