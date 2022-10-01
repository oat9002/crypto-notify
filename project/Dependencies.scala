import sbt._

object Dependencies {
  val akka = "2.6.20"
  val akkaHttp = "10.2.10"
  val typesafeConfig = "1.4.1"
  val akkaQuartz = "1.9.3-akka-2.6.x"
  val jackson = "2.13.4"
  val macWire = "2.5.8"
  val scalaLogging = "3.9.4"
  val slf4j = "1.7.32"
  val scalaTest = "3.2.10"
  val scalaMock = "5.2.0"
  val sttp = "3.8.0"
  val web3j = "4.9.1"

  lazy val dependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akka,
    "com.typesafe.akka" %% "akka-stream" % akka,
    "com.typesafe.akka" %% "akka-http" % akkaHttp cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttp cross CrossVersion.for3Use2_13,
    "com.typesafe" % "config" % typesafeConfig,
    "com.enragedginger" %% "akka-quartz-scheduler" % akkaQuartz,
    "com.fasterxml.jackson.core" % "jackson-databind" % jackson,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jackson,
    "com.softwaremill.macwire" %% "macros" % macWire % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % macWire % "provided" cross CrossVersion.for3Use2_13,
    "com.softwaremill.macwire" %% "util" % macWire,
    "com.softwaremill.macwire" %% "proxy" % macWire,
    "org.slf4j" % "slf4j-simple" % slf4j,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging,
    "com.softwaremill.sttp.client3" %% "core" % sttp,
    "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttp cross CrossVersion.for3Use2_13,
    "org.web3j" % "core" % web3j
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTest % "test",
    "org.scalatest" %% "scalatest-shouldmatchers" % scalaTest % "test",
    "org.scalamock" %% "scalamock" % scalaMock % "test" cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akka % "test"
  )

  lazy val allDependencies: Seq[ModuleID] = dependencies ++ testDependencies
}
