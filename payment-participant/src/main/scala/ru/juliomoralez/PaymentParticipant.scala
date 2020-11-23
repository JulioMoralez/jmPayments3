package ru.juliomoralez

import java.io.File

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.localrunner.LocalRunner.log
import cloudflow.streamlets.avro.AvroInlet
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import com.typesafe.config.ConfigFactory
import juliomoralez.data.Payment
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.PaymentParticipant.process
import ru.juliomoralez.configs.Config.{config, defaultUserBalance, usersConfigFilePath}
import ru.juliomoralez.configs.UsersConfig

import scala.collection.mutable

class PaymentParticipant extends FlinkStreamlet with Serializable {
  @transient val in: AvroInlet[Payment] = AvroInlet[Payment]("in")
  @transient val shape: StreamletShape  = StreamletShape(in)

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        val defBalance        = context.streamletConfig.getInt(defaultUserBalance.key)
        val usersConfFilePath = context.streamletConfig.getString(usersConfigFilePath.key)
        val usersStartBalance = UsersConfig(ConfigFactory.parseFile(new File(usersConfFilePath)))
        readStream(in).map(payment => process(payment, usersStartBalance, defBalance))
      } catch {
        case e: Exception =>
          log.error("PaymentParticipant error", e)
          throw e
      }
    }
  }
}

object PaymentParticipant {
  val users: mutable.Map[String, Int] = mutable.Map()

  def createUser(names: Vector[String], usersStartBalance: Map[String, Int], defBalance: Int): Unit = {
    names.foreach(name =>
      if (!users.contains(name)) {
        val startBalance: Int = if (usersStartBalance.contains(name)) {
          usersStartBalance(name)
        } else {
          defBalance
        }
        users += (name -> startBalance)
        log.info(s"User $name created. Start balance = $startBalance")
      }
    )
  }

  def process(payment: Payment, usersStartBalance: Map[String, Int], defBalance: Int): Unit = {
    log.info(payment.toString)
    createUser(Vector(payment.from, payment.to), usersStartBalance, defBalance)
    val newBalanceFrom = users(payment.from) - payment.value
    if (newBalanceFrom >= 0) {
      val newBalanceTo = users(payment.to) + payment.value
      users(payment.from) = newBalanceFrom
      users(payment.to) = newBalanceTo
      log.info(s"Payment successful. New balance: ${payment.from}=$newBalanceFrom, ${payment.to}=$newBalanceTo")
    } else {
      log.info("Canceling a payment")
    }
  }
}
