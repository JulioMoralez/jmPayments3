package ru.juliomoralez

import java.io.File

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.localrunner.LocalRunner.log
import cloudflow.streamlets.avro.AvroInlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import com.typesafe.config.ConfigFactory
import juliomoralez.data.Payment
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.configs.Config.{config, defaultUserBalanceConf, usersConfigFilePathConf}
import ru.juliomoralez.configs.UsersBalanceConfig

class PaymentParticipant extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Payment] = AvroInlet[Payment]("in")
  @transient val shape: StreamletShape  = StreamletShape(in)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val defaultUserBalance  = context.streamletConfig.getInt(defaultUserBalanceConf.key)
        val usersConfigFilePath = context.streamletConfig.getString(usersConfigFilePathConf.key)
        val usersStartBalance   = UsersBalanceConfig(ConfigFactory.parseFile(new File(usersConfigFilePath)))
        readStream(in).keyBy(0).map(new UserRichFunction(usersStartBalance, defaultUserBalance))
      } catch {
        case e: Exception =>
          log.error("PaymentParticipant error", e)
          throw e
      }
    }
  }
}