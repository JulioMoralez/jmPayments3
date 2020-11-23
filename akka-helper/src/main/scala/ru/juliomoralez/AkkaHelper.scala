package ru.juliomoralez

import java.nio.file.{Files, Paths}
import java.util.UUID

import akka.stream.scaladsl.{RunnableGraph, Sink}
import cloudflow.akkastream.AkkaStreamlet
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets.avro.AvroInlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import juliomoralez.data.Message
import ru.juliomoralez.configs.Config.{config, defaultPaymentFileDirConf}

class AkkaHelper extends AkkaStreamlet with Serializable {
  @transient val in: AvroInlet[Message] = AvroInlet[Message]("in")
  @transient val shape: StreamletShape  = StreamletShape(in)

  override def configParameters: Vector[ConfigParameter] = config

  final override def createLogic: RunnableGraphStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = {
      try {
        val defaultPaymentFileDir = context.streamletConfig.getString(defaultPaymentFileDirConf.key)
        val pathDir = Paths.get(defaultPaymentFileDir)

        plainSource(in).to(Sink.foreach(receiveMsg => {
          val nameFile = receiveMsg.text
          val pathFile = Paths.get(nameFile)

          if (Files.exists(pathFile)) {
            if (Files.notExists(pathDir)) {
              Files.createDirectories(pathDir)
            }
            Files.list(pathDir).forEach(Files.delete(_))
            Files.copy(pathFile, Paths.get(s"$defaultPaymentFileDir/${UUID.randomUUID}"))
            log.info(s"File prepared for reading: $nameFile")
          } else {
            log.info(s"Missing file: $nameFile")
          }
        }))
      }  catch {
        case e: Exception =>
          log.error("AkkaHelper error", e)
          throw e
      }
    }
  }
}
