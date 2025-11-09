import sbt.*

object Dependencies {
  val pekko = "1.1.5"
  val pekkoHttp = "1.3.0"
  val pekkoHttpCircie = "3.6.0"
  val typesafeConfig = "1.4.5"
  val pekkoQuartz = "1.3.0-pekko-1.1.x"
  val macWire = "2.5.8"
  val scalaLogging = "3.9.6"
  val slf4j = "2.0.17"
  val logback = "1.5.20"
  val scalaTest = "3.2.19"
  val sttp = "3.11.0"
  val web3j = "4.10.3"
  val circe = "0.14.15"
  val retry = "0.3.6"

  lazy val dependencies: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-actor-typed" % pekko cross CrossVersion.for3Use2_13 excludeAll
      (ExclusionRule(organization = "org.slf4j"), ExclusionRule("com.typesafe", "config")),
    "org.apache.pekko" %% "pekko-stream" % pekko cross CrossVersion.for3Use2_13,
    "org.apache.pekko" %% "pekko-http" % pekkoHttp cross CrossVersion.for3Use2_13,
    "com.github.pjfanning" %% "pekko-http-circe" % pekkoHttpCircie cross CrossVersion.for3Use2_13 excludeAll ExclusionRule(
      organization = "io.circe"
    ),
    "io.github.samueleresca" % "pekko-quartz-scheduler" % pekkoQuartz cross CrossVersion.for3Use2_13 excludeAll
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
    "com.softwaremill.sttp.client3" % "pekko-http-backend" % sttp cross CrossVersion.for3Use2_13,
    "com.softwaremill.sttp.client3" %% "circe" % sttp cross CrossVersion.for3Use2_13 excludeAll
      ExclusionRule(organization = "io.circe"),
    "org.web3j" % "core" % web3j excludeAll
      ExclusionRule(organization = "org.slf4j"),
    "com.softwaremill.retry" %% "retry" % retry
  )

  lazy val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTest % "test",
    "org.scalatest" %% "scalatest-shouldmatchers" % scalaTest % "test",
    "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekko % "test" cross CrossVersion.for3Use2_13
  )

  lazy val allDependencies: Seq[ModuleID] = dependencies ++ testDependencies
}
