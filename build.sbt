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

name := "crypto-notify"

scalaVersion := "2.13.7"

libraryDependencies ++= Dependencies.allDependencies

enablePlugins(JavaAppPackaging, DockerPlugin)

dockerRepository := Some("oat9002")
dockerBaseImage := "eclipse-temurin:18-jre-focal"
dockerExposedPorts := Seq(8080, 80, 443)
dockerUpdateLatest := true

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
