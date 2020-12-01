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
  val strategyRestartAttemptsConf: ConfigParameter = IntegerConfigParameter(
    "strategy-restart-attempts",
    "The number of times that Flink retries the execution before the job is declared as failed"
  )
  val strategyDelayIntervalConf: ConfigParameter = IntegerConfigParameter(
    "strategy-delay-interval",
    "Delay between two consecutive restart attempts"
  )
  val checkpointDataUriConf: ConfigParameter = StringConfigParameter(
    "checkpoint-data-uri",
    "Directory used for storing the data files"
  )
  val checkpointDelayIntervalConf: ConfigParameter = IntegerConfigParameter(
    "checkpoint-delay-interval",
    "The checkpoint interval in milliseconds"
  )

  val config: Vector[ConfigParameter] = Vector(
    defaultUserBalanceConf,
    usersConfigFilePathConf,
    strategyRestartAttemptsConf,
    strategyDelayIntervalConf,
    checkpointDataUriConf,
    checkpointDelayIntervalConf
  )
}
