import Dependencies._

name := "crypto-notify"

version := "1.0"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akka,
  "com.typesafe.akka" %% "akka-stream" % akka,
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttp,
  "com.typesafe" % "config" % typesafeConfig,
  "org.jobrunr" % "jobrunr" % jobrunr,
  "com.enragedginger" %% "akka-quartz-scheduler" % akkaQuartz,
  "com.fasterxml.jackson.core" % "jackson-databind" % jackson,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jackson,
  "com.softwaremill.macwire" %% "macros" % macWire % "provided",
  "com.softwaremill.macwire" %% "macrosakka" % macWire % "provided",
  "com.softwaremill.macwire" %% "util" % macWire,
  "com.softwaremill.macwire" %% "proxy" % macWire
)

enablePlugins(JavaAppPackaging, DockerPlugin)

dockerRepository := Some("oat9002")
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true

