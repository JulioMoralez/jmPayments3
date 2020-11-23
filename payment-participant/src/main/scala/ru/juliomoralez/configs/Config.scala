package ru.juliomoralez.configs

import cloudflow.streamlets.{ ConfigParameter, IntegerConfigParameter, StringConfigParameter }

object Config {
  val defaultUserBalance: ConfigParameter = IntegerConfigParameter(
    "default-user-balance",
    "The default starting value of the user's balance"
  )
  val usersConfigFilePath: ConfigParameter = StringConfigParameter(
    "users-config-file-path",
    "File path to user config"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultUserBalance,
    usersConfigFilePath
  )
}
