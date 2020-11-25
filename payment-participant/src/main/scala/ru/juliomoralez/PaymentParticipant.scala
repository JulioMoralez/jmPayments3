package ru.juliomoralez

import java.io.File

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import com.typesafe.config.ConfigFactory
import juliomoralez.data.{LogMessage, Payment}
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.configs.Config.{config, defaultUserBalanceConf, usersConfigFilePathConf}
import ru.juliomoralez.configs.UsersBalanceConfig

class PaymentParticipant extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Payment] = AvroInlet[Payment]("in")
  @transient val out: AvroOutlet[LogMessage] = AvroOutlet[LogMessage]("out")
  @transient val shape: StreamletShape  = StreamletShape(in).withOutlets(out)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val defaultUserBalance  = context.streamletConfig.getInt(defaultUserBalanceConf.key)
        val usersConfigFilePath = context.streamletConfig.getString(usersConfigFilePathConf.key)
        val usersStartBalance   = UsersBalanceConfig(ConfigFactory.parseFile(new File(usersConfigFilePath)))
        val logMessage = readStream(in).keyBy(0).flatMap(new UserRichFunction(usersStartBalance, defaultUserBalance))
        writeStream(out, logMessage)
      } catch {
        case e: Exception =>
          log.error("PaymentParticipant error", e)
          throw e
      }
    }
  }
}