package ru.juliomoralez.configs

import cloudflow.streamlets.{ ConfigParameter, StringConfigParameter }

object Config {
  val paymentRegexConf: ConfigParameter = StringConfigParameter(
    "payment-regex",
    "Payment mask"
  )

  val config: Vector[ConfigParameter] = Vector(
    paymentRegexConf
  )
}
