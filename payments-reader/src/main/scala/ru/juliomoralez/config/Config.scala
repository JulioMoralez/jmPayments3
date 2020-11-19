package ru.juliomoralez.config

import cloudflow.streamlets.{ConfigParameter, StringConfigParameter}

object Config {
  val fileDir: ConfigParameter = StringConfigParameter(
    "file-dir"
  )
  val fileRegex: ConfigParameter = StringConfigParameter(
    "file-regex"
  )

  val config: Vector[ConfigParameter] = Vector(
    fileDir,
    fileRegex
  )
}
