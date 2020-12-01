package ru.juliomoralez

import cloudflow.flink.testkit._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.streaming.api.scala._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.juliomoralez.data.{LogLevel, LogMessage, Message, Payment}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

class PaymentCheckerTest extends FlinkTestkit
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  "paymentChecker" should {
    "check inner messages" in {
      @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment

      val paymentChecker = new PaymentChecker()

      val data = Message(s"Bob => Max : 100") +: (1 to 3).map(i => Message(s"Bob -> Max : $i"))

      val in = inletAsTap[Message](
        paymentChecker.in,
        env.addSource(FlinkSource.CollectionSourceFunction(data)))

      val outValid = outletAsTap[Payment](paymentChecker.outValid)
      val outInvalid = outletAsTap[LogMessage](paymentChecker.outInvalid)

      run(paymentChecker, Seq(in), Seq(outValid, outInvalid), env)

      TestFlinkStreamletContext.result should contain(LogMessage(LogLevel.WARNING, "[Bob => Max : 100] incorrect message").toString)
      TestFlinkStreamletContext.result should contain(Payment(0, "Bob", "Max", 1).toString)
      TestFlinkStreamletContext.result.size should equal(4)
    }
  }

  override def config: Config = {
    val map = Map("cloudflow.streamlets.testFlinkStreamlet.payment-regex" -> "([A-Za-z0-9]+) (->) ([A-Za-z0-9]+) (:) ([0-9]+)")
    ConfigFactory.parseMap(map.asJava)
  }
}
