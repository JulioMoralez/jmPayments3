package ru.juliomoralez.configs

import cloudflow.streamlets.{BooleanConfigParameter, ConfigParameter, StringConfigParameter}

object Config {
  val fileDir: ConfigParameter = StringConfigParameter(
    "file-dir",
    "Directory name with payment files"
  )
  val fileRegex: ConfigParameter = StringConfigParameter(
    "file-regex",
    "Payment files mask"
  )
  val defaultPaymentFileDirConf: ConfigParameter = StringConfigParameter(
    "default-payment-file-dir",
    "Default directory where payment file is located"
  )
  val paymentFromHttpConf: ConfigParameter = BooleanConfigParameter(
    "payment-from-http",
    "Selection source payment. " +
      "True - from Http, example. {\"text\":\"file1.txt\"}." +
      "False - from 'fileDir' with 'fileRegex' mask"
  )

  val config: Vector[ConfigParameter] = Vector(
    fileDir,
    fileRegex,
    defaultPaymentFileDirConf,
    paymentFromHttpConf
  )
}
