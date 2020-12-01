package ru.juliomoralez

import java.io.File

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletContext, FlinkStreamletLogic}
import cloudflow.streamlets.avro.{AvroInlet, AvroOutlet}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.runtime.state.StateBackend
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import ru.juliomoralez.PaymentParticipant.configureCheckpointing
import ru.juliomoralez.configs.Config._
import ru.juliomoralez.configs.UsersBalanceConfig
import ru.juliomoralez.data.{LogMessage, Payment}

class PaymentParticipant extends FlinkStreamlet {
  @transient val in: AvroInlet[Payment] = AvroInlet[Payment]("in")
  @transient val out: AvroOutlet[LogMessage] = AvroOutlet[LogMessage]("out")
  @transient val shape: StreamletShape  = StreamletShape(in).withOutlets(out)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        configureCheckpointing(context)

        val defaultUserBalance  = context.streamletConfig.getInt(defaultUserBalanceConf.key)
        val usersConfigFilePath = context.streamletConfig.getString(usersConfigFilePathConf.key)
        val usersStartBalance   = UsersBalanceConfig(ConfigFactory.parseFile(new File(usersConfigFilePath))) //Map("Sam" -> 100)
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

object PaymentParticipant extends Serializable {

  def configureCheckpointing(context: FlinkStreamletContext): Unit = {
    val strategyRestartAttempts  = context.streamletConfig.getInt(strategyRestartAttemptsConf.key)
    val strategyDelayInterval  = context.streamletConfig.getInt(strategyDelayIntervalConf.key)
    val checkpointDataUri  = context.streamletConfig.getString(checkpointDataUriConf.key)
    val checkpointDelayInterval  = context.streamletConfig.getInt(checkpointDelayIntervalConf.key)
    val env = context.env

    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(strategyRestartAttempts, strategyDelayInterval))
    env.setStateBackend(new FsStateBackend(checkpointDataUri).asInstanceOf[StateBackend])
    env.enableCheckpointing(checkpointDelayInterval)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
  }
}