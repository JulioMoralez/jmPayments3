package ru.juliomoralez.configs

import com.typesafe.config.Config

import scala.collection.JavaConverters.asScalaBufferConverter

object UsersBalanceConfig {
  def apply(conf: Config): Map[String, Int] = {
    conf.getConfigList("users").asScala
      .map(u => u.getString("name").trim -> u.getInt("balance")).toMap
  }
}
