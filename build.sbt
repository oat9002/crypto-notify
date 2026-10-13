import com.typesafe.sbt.packager.docker.Cmd
import sbtrelease.ReleaseStateTransformations.*

ThisBuild / scalaVersion := "3.8.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "crypto-notify",
    libraryDependencies ++= Dependencies.allDependencies
  )

scalacOptions ++= Seq("-Xmax-inlines", "50")
scalacOptions ++= Seq("-deprecation")

enablePlugins(JavaAppPackaging, DockerPlugin)

dockerRepository := Some("oat9002")
dockerBaseImage := "azul/zulu-openjdk:25-jre-latest"
dockerExposedPorts := Seq(8080, 80, 443)
dockerUpdateLatest := true
dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("USER", args @ _*) if !args.contains("root") =>
    Seq(
      Cmd("USER", "root"),
      Cmd("RUN", "apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*"),
      cmd
    )
  case other => Seq(other)
}

releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStepdocker
  runClean, // : ReleaseStep
  runTest, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)
releaseUseGlobalVersion := false
releaseIgnoreUntrackedFiles := true
