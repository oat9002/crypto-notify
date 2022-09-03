import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import sbtrelease.ReleaseStateTransformations.{
  checkSnapshotDependencies,
  commitNextVersion,
  commitReleaseVersion,
  inquireVersions,
  pushChanges,
  runClean,
  runTest,
  setNextVersion,
  setReleaseVersion,
  tagRelease
}

import scala.sys.process.Process

name := "crypto-notify"

scalaVersion := "2.13.5"

libraryDependencies ++= Dependencies.allDependencies

enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val ensureDockerBuildx =
  taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx =
  taskKey[Unit]("Build docker images using buildx")
lazy val dockerBuildxSettings = Seq(
  ensureDockerBuildx := {
    if (Process("docker buildx inspect multi-arch-builder").! == 1) {
      Process(
        "docker buildx create --use --name multi-arch-builder",
        baseDirectory.value
      ).!
    }
  },
  dockerBuildWithBuildx := {
    streams.value.log("Building and pushing image with Buildx")
    dockerAliases.value.foreach(alias =>
      Process(
        "docker buildx build --platform=linux/arm64,linux/amd64 --push -t " +
          alias + " .",
        baseDirectory.value / "target" / "docker" / "stage"
      ).!
    )
  },
  publish in Docker := Def
    .sequential(
      publishLocal in Docker,
      ensureDockerBuildx,
      dockerBuildWithBuildx
    )
    .value
)

dockerRepository := Some("oat9002")
dockerBaseImage := "openjdk:16-slim"
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  ExecCmd("RUN", "apk", "--no-cache", "add", "bash"),
  ExecCmd("RUN", "apk", "--no-cache", "add", "curl")
)
dockerExposedPorts := Seq(8080, 80, 443)
dockerUpdateLatest := true

releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
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
