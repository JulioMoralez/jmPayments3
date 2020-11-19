package ru.juliomoralez.config

import cloudflow.streamlets.{ConfigParameter, IntegerConfigParameter}

object Config {
  val defaultUserBalance: ConfigParameter = IntegerConfigParameter(
    "default-user-balance",
    "Стартовое значаение баланса пользователя по умолчанию"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultUserBalance
  )
}
