package ru.juliomoralez.config

import scala.collection.JavaConverters.asScalaBufferConverter

final case class UsersConfig(usersStartBalance: Map[String, Int])

object UsersConfig {
  def apply(config: com.typesafe.config.Config): UsersConfig = {
    val usersStartBalance: Map[String, Int] = config.getConfigList("users").asScala
      .map(u => u.getString("name").trim -> u.getInt("balance")).toMap
    new UsersConfig(usersStartBalance)
  }
}
