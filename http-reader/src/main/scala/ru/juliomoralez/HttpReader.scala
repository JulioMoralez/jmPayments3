package ru.juliomoralez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cloudflow.akkastream.AkkaServerStreamlet
import cloudflow.akkastream.util.scaladsl.HttpServerLogic
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroOutlet
import juliomoralez.data.Message
import ru.juliomoralez.JsonFormats.MessageJsonFormat

class HttpReader extends AkkaServerStreamlet with Serializable {
  @transient val out: AvroOutlet[Message] = AvroOutlet[Message]("out")
  @transient val shape: StreamletShape = StreamletShape.withOutlets(out)

  final override def createLogic: HttpServerLogic = HttpServerLogic.default(this, out)
}
