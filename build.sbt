import sbt._
import sbt.Keys._

val logbackVersion = "1.2.3"
val scalatestVersion = "3.0.8"

lazy val root =
  Project(id = "root", base = file("."))
    .enablePlugins(ScalafmtPlugin)
    .settings(
      name := "root",
      scalafmtOnCompile := true,
      skip in publish := true
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      paymentSystemPipeline,
      datamodel,
      httpReader,
      paymentsReader,
      paymentChecker,
      paymentParticipant,
      logIncorrectPayment
    )

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(CloudflowLibraryPlugin)

lazy val paymentSystemPipeline = (project in file("./payment-system-pipeline"))
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(
    commonSettings,
    runLocalConfigFile := Some("payment-system-pipeline/src/main/resources/local.conf"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
      )
  )

lazy val httpReader = (project in file("./http-reader"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"       % scalatestVersion  % "test"
    )
  )
  .dependsOn(datamodel)

lazy val paymentsReader = (project in file("./payments-reader"))
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"       % scalatestVersion  % "test"
    )
  )
  .dependsOn(datamodel)

lazy val paymentChecker = (project in file("./payment-checker"))
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"       % scalatestVersion  % "test"
    )
  )
  .dependsOn(datamodel)

lazy val paymentParticipant = (project in file("./payment-participant"))
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"       % scalatestVersion  % "test"
    )
  )
  .dependsOn(datamodel)

lazy val logIncorrectPayment = (project in file("./log-incorrect-payment"))
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"       % scalatestVersion  % "test"
    )
  )
  .dependsOn(datamodel)

lazy val commonSettings = Seq(
  organization := "com.lightbend.cloudflow",
  headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),

  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

)

dynverSeparator in ThisBuild := "-"
