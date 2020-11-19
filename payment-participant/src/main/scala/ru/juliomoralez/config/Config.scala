package ru.juliomoralez.config

import cloudflow.streamlets.{ConfigParameter, IntegerConfigParameter}

object Config {
  val defaultUserBalance: ConfigParameter = IntegerConfigParameter(
    "default-user-balance"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultUserBalance
  )
}
