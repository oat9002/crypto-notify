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

ThisBuild / scalaVersion := "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "crypto-notify",
    libraryDependencies ++= Dependencies.allDependencies
  )

scalacOptions ++= Seq("-Xmax-inlines", "50")

enablePlugins(JavaAppPackaging, DockerPlugin)

dockerRepository := Some("oat9002")
dockerBaseImage := "eclipse-temurin:17-jre-focal"
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
