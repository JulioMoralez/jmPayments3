package ru.juliomoralez.config

import cloudflow.streamlets.{ConfigParameter, StringConfigParameter}

object Config {
  val paymentRegex: ConfigParameter = StringConfigParameter(
    "payment-regex"
  )

  val config: Vector[ConfigParameter] = Vector(
    paymentRegex
  )
}
