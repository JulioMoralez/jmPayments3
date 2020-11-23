package ru.juliomoralez.configs

import cloudflow.streamlets.{ ConfigParameter, StringConfigParameter }

object Config {
  val defaultPaymentFileDirConf: ConfigParameter = StringConfigParameter(
    "default-payment-file-dir",
    "Default directory where payment file is located"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultPaymentFileDirConf
  )
}
