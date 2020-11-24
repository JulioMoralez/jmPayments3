package ru.juliomoralez

import cloudflow.localrunner.LocalRunner.log
import juliomoralez.data.Payment
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}

class UserRichFunction(usersStartBalance: Map[String, Int], defaultUserBalance: Int) extends RichMapFunction[Payment, Unit] {
  @transient lazy val users: MapState[String, Int] = getRuntimeContext.getMapState(new MapStateDescriptor[String, Int]("users", classOf[String], classOf[Int]))

  override def map(payment: Payment): Unit = {
    log.info(payment.toString)
    createUser(Vector(payment.from, payment.to))
    val newBalanceFrom = users.get(payment.from) - payment.value
    if (newBalanceFrom >= 0) {
      val newBalanceTo = users.get(payment.to) + payment.value
      users.put(payment.from, newBalanceFrom)
      users.put(payment.to, newBalanceTo)
      log.info(s"Payment successful. New balance: ${payment.from}=$newBalanceFrom, ${payment.to}=$newBalanceTo")
    } else {
      log.info("Canceling a payment")
    }
  }

  def createUser(names: Vector[String]): Unit = {
    names.foreach(name =>
      if (!users.contains(name)) {
        val startBalance: Int = if (usersStartBalance.contains(name)) {
          usersStartBalance(name)
        } else {
          defaultUserBalance
        }
        users.put(name, startBalance)
        log.info(s"User $name created. Start balance = $startBalance")
      }
    )
  }
}
