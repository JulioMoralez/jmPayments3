package ru.juliomoralez.configs

import cloudflow.streamlets.{ConfigParameter, StringConfigParameter}

object Config {
  val fileDirConf: ConfigParameter = StringConfigParameter(
    "file-dir",
    "Directory name with payment files"
  )
  val fileRegexConf: ConfigParameter = StringConfigParameter(
    "file-regex",
    "Payment files mask"
  )
  val defaultPaymentFileDirConf: ConfigParameter = StringConfigParameter(
    "default-payment-file-dir",
    "Default directory where payment file is located"
  )
  val sourcePaymentConf: ConfigParameter = StringConfigParameter(
    "source-payment",
    "Selection source payment. " +
      "'http' - from Http, use POST request method" +
      "'file' - from 'fileDir' with 'fileRegex' mask"
  )

  val config: Vector[ConfigParameter] = Vector(
    fileDirConf,
    fileRegexConf,
    defaultPaymentFileDirConf,
    sourcePaymentConf
  )
}
