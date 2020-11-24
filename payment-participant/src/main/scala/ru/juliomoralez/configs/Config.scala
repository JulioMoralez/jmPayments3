package ru.juliomoralez.configs

import cloudflow.streamlets.{ ConfigParameter, IntegerConfigParameter, StringConfigParameter }

object Config {
  val defaultUserBalanceConf: ConfigParameter = IntegerConfigParameter(
    "default-user-balance",
    "The default starting value of the user's balance"
  )
  val usersConfigFilePathConf: ConfigParameter = StringConfigParameter(
    "users-config-file-path",
    "File path to user config"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultUserBalanceConf,
    usersConfigFilePathConf
  )
}
