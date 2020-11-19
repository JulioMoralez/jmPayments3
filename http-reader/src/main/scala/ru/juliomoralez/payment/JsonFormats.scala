package ru.juliomoralez.payment

import juliomoralez.data.Message
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

object JsonFormats extends DefaultJsonProtocol {

  implicit object MessageJsonFormat extends RootJsonFormat[Message] {
    def write(t: Message): JsValue = JsObject(
      "text" -> JsString(t.text)
    )
    def read(value: JsValue): Message =
      value.asJsObject.getFields("text") match {
        case Seq(JsString(text)) =>
          new Message(text)
        case _ => throw DeserializationException("Message expected")
      }
  }
}
