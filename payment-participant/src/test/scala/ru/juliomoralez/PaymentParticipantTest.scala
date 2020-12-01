package ru.juliomoralez

import cloudflow.flink.testkit._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.streaming.api.scala._
import org.scalatest._
import ru.juliomoralez.data.{LogLevel, LogMessage, Payment}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

class PaymentParticipantTest extends FlinkTestkit
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  "paymentParticipant" should {
    "execute payment transactions" in {
      @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment

      val paymentParticipant = new PaymentParticipant()

      val data = Payment(0, "a", "b", 1000) +: (1 to 2).map(i => Payment(0, "a", "b", i))

      val in = inletAsTap[Payment](
        paymentParticipant.in,
        env.addSource(FlinkSource.CollectionSourceFunction(data)))

      val out = outletAsTap[LogMessage](paymentParticipant.out)

      run(paymentParticipant, Seq(in), Seq(out), env)

      TestFlinkStreamletContext.result should contain(LogMessage(LogLevel.WARNING, "[a -> b: 1000] Canceling a payment").toString)
      TestFlinkStreamletContext.result should contain(LogMessage(LogLevel.INFO, "[a -> b: 1] Payment successful. New balance: a=99, b=101").toString)
      TestFlinkStreamletContext.result.size should equal(3)
    }
  }

  override def config: Config = {
    val prefix = "cloudflow.streamlets.testFlinkStreamlet"
    val map = Map(
      s"$prefix.default-user-balance" -> "100",
      s"$prefix.users-config-file-path" -> "payment-participant/src/main/resources/users.conf",
      s"$prefix.strategy-restart-attempts" -> "3",
      s"$prefix.strategy-delay-interval" -> "1000",
      s"$prefix.checkpoint-data-uri" -> "file:///data/jmPayments/checkpoints",
      s"$prefix.checkpoint-delay-interval" -> "1000"
    )
    ConfigFactory.parseMap(map.asJava)
  }

}