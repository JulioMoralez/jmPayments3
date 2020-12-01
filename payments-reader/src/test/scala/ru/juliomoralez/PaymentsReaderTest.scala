package ru.juliomoralez

import cloudflow.flink.testkit._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.streaming.api.scala._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.juliomoralez.data.{LogLevel, LogMessage, Message, Payment}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

class PaymentsReaderTest extends FlinkTestkit
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  "paymentsReader" should {
    "read messages from file" in {
      @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment

      val paymentsReader = new PaymentsReader()

      val out = outletAsTap[Message](paymentsReader.out)

      run(paymentsReader, Seq(), Seq(out), env)

      TestFlinkStreamletContext.result should contain(Message("Bob -> Max : 1").toString)
      TestFlinkStreamletContext.result.size should equal(10)
    }
  }


  override def config: Config = {
    val prefix = "cloudflow.streamlets.testFlinkStreamlet"
    val map = Map(
      s"$prefix.file-dir" -> ".",
      s"$prefix.file-regex" -> "file[0-9]+.txt",
      s"$prefix.default-payment-file-dir" -> "data/",
      s"$prefix.source-payment" -> "file"
    )
    ConfigFactory.parseMap(map.asJava)
  }
}
