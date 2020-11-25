package ru.juliomoralez

import cloudflow.localrunner.LocalRunner.log
import juliomoralez.data.{LogLevel, LogMessage, Payment}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.util.Collector

import scala.util.Random

final case class User(name: String, balance: Int, age: Int)

class UserRichFunction(usersStartBalance: Map[String, Int], defaultUserBalance: Int) extends RichFlatMapFunction[Payment, LogMessage] {
  @transient lazy val users: MapState[String, User] = getRuntimeContext.getMapState(new MapStateDescriptor[String, User]("users", classOf[String], classOf[User]))

  override def flatMap(payment: Payment, out: Collector[LogMessage]): Unit = {
        log.info(payment.toString)
        checkNewUsers(Vector(payment.from, payment.to))
        val message = s"${payment.from} -> ${payment.to}: ${payment.value}"
        val userFrom = users.get(payment.from)
        val newBalanceFrom = userFrom.balance - payment.value
        if (newBalanceFrom >= 0) {
          val userTo = users.get(payment.to)
          val newBalanceTo = userTo.balance + payment.value
          users.put(payment.from, User(userFrom.name, newBalanceFrom, userFrom.age))
          users.put(payment.to, User(userTo.name, newBalanceTo, userTo.age))
          out.collect(LogMessage(LogLevel.INFO, s"[$message] Payment successful. New balance: ${payment.from}=$newBalanceFrom, ${payment.to}=$newBalanceTo"))
        } else {
          out.collect(LogMessage(LogLevel.WARNING, s"[$message] Canceling a payment"))
        }
  }

  def checkNewUsers(names: Vector[String]): Unit = {
    names.foreach(name =>
      if (!users.contains(name)) {
        val startBalance: Int = if (usersStartBalance.contains(name)) {
          usersStartBalance(name)
        } else {
          defaultUserBalance
        }
        users.put(name, User(name, startBalance, Random.nextInt(90) + 18))
        log.info(s"User $name created. Start balance = $startBalance")
      }
    )
  }
}