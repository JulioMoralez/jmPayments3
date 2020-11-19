package ru.juliomoralez.payment

import java.io.File

import cloudflow.flink.{FlinkStreamlet, FlinkStreamletLogic}
import cloudflow.streamlets.{ConfigParameter, StreamletShape}
import cloudflow.streamlets.avro.AvroInlet
import com.typesafe.config.ConfigFactory
import juliomoralez.data.Payment
import org.apache.flink.api.scala.createTypeInformation
import ru.juliomoralez.config.Config.{config, defaultUserBalance}
import ru.juliomoralez.config.UsersConfig
import ru.juliomoralez.util.Const.USERS_CONFIG_FILE_PATH

import scala.collection.mutable

object PaymentParticipant extends FlinkStreamlet{
  val in: AvroInlet[Payment] = AvroInlet[Payment]("in")
  val shape: StreamletShape = StreamletShape(in)

  val users: mutable.Map[String, Int] = mutable.Map()
  val usersConfig: UsersConfig = UsersConfig(ConfigFactory.parseFile(new File(USERS_CONFIG_FILE_PATH)))
  lazy val defBalance: Int = context.streamletConfig.getInt(defaultUserBalance.key)

  def createUser(names: Vector[String]): Unit = {
    names.foreach(name =>
      if (!users.contains(name)) {
        val startBalance: Int = if (usersConfig.usersStartBalance.contains(name)) {
          usersConfig.usersStartBalance(name)
        } else {
          defBalance
        }
        users += (name -> startBalance)
        log.info(s"Создан $name. Стартовый баланс $startBalance")
      }
    )
  }

  def process: Payment => Unit = { payment =>
    log.info(payment.toString)
    createUser(Vector(payment.from, payment.to))
    val newBalanceFrom = users(payment.from) - payment.value
    if (newBalanceFrom >= 0) {
      val newBalanceTo = users(payment.to) + payment.value
      users(payment.from) = newBalanceFrom
      users(payment.to) = newBalanceTo
      log.info(s"Операция успешна. Новый баланс ${payment.from}=$newBalanceFrom, ${payment.to}=$newBalanceTo")
    } else {
      log.info("Отмена операции")
    }
  }

  override def configParameters: Vector[ConfigParameter] = config

  override def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {

    override def buildExecutionGraph: Unit = {
      try {
        readStream(in).map(process)
      } catch {
        case e: Exception =>
          log.error("PaymentParticipant error", e)
          throw e
      }
    }
  }
}
