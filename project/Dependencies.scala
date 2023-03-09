import sbt._

object Dependencies {
  val akka = "2.6.20"
  val akkaHttp = "10.2.10"
  val akkaHttpCircie = "1.39.2"
  val typesafeConfig = "1.4.1"
  val akkaQuartz = "1.9.3-akka-2.6.x"
  val macWire = "2.5.8"
  val scalaLogging = "3.9.4"
  val slf4j = "2.0.3"
  val logback = "1.4.5"
  val scalaTest = "3.2.10"
  val sttp = "3.8.2"
  val web3j = "4.9.1"
  val circe = "0.14.3"
  val retry = "0.3.6"

  lazy val dependencies: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akka cross CrossVersion.for3Use2_13 excludeAll
      (ExclusionRule(organization = "org.slf4j"), ExclusionRule("com.typesafe", "config")),
    "com.typesafe.akka" %% "akka-stream" % akka cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-http" % akkaHttp cross CrossVersion.for3Use2_13,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCircie cross CrossVersion.for3Use2_13 excludeAll (ExclusionRule(
      organization = "io.circe"
    ),
    ExclusionRule("com.typesafe.akka", "akka-http")),
    "com.enragedginger" % "akka-quartz-scheduler" % akkaQuartz cross CrossVersion.for3Use2_13 excludeAll
      ExclusionRule(organization = "org.slf4j"),
    "com.typesafe" % "config" % typesafeConfig,
    "io.circe" %% "circe-core" % circe,
    "io.circe" %% "circe-generic" % circe,
    "io.circe" %% "circe-parser" % circe,
    "org.slf4j" % "slf4j-simple" % slf4j,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging excludeAll
      ExclusionRule(organization = "org.slf4j"),
    "ch.qos.logback" % "logback-classic" % logback,
    "com.softwaremill.sttp.client3" %% "core" % sttp cross CrossVersion.for3Use2_13,
    "com.softwaremill.sttp.client3" % "akka-http-backend" % sttp cross CrossVersion.for3Use2_13,
    "com.softwaremill.sttp.client3" %% "circe" % sttp cross CrossVersion.for3Use2_13 excludeAll
      ExclusionRule(organization = "io.circe"),
    "org.web3j" % "core" % web3j excludeAll
      ExclusionRule(organization = "org.slf4j"),
    "com.softwaremill.retry" %% "retry" % retry
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTest % "test",
    "org.scalatest" %% "scalatest-shouldmatchers" % scalaTest % "test",
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akka % "test" cross CrossVersion.for3Use2_13
  )

  lazy val allDependencies: Seq[ModuleID] = dependencies ++ testDependencies
}
