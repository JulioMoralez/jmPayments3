package ru.juliomoralez.reader

import cloudflow.flink.FlinkStreamletContext
import juliomoralez.data.Message
import org.apache.flink.streaming.api.scala.DataStream

trait ReaderFactory {
  def readPayment(context: FlinkStreamletContext): DataStream[Message]
}
