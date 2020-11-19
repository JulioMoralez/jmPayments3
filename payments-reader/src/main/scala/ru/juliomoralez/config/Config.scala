package ru.juliomoralez.config

import cloudflow.streamlets.{ConfigParameter, StringConfigParameter}

object Config {
  val fileDir: ConfigParameter = StringConfigParameter(
    "file-dir",
    "Директория с файлами платежей"
  )
  val fileRegex: ConfigParameter = StringConfigParameter(
    "file-regex",
    "Маска файлов платежей"
  )

  val config: Vector[ConfigParameter] = Vector(
    fileDir,
    fileRegex
  )
}
