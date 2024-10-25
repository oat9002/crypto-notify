import sbtrelease.ReleaseStateTransformations.*

ThisBuild / scalaVersion := "3.3.4"

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
dockerBaseImage := "eclipse-temurin:21-jre-jammy"
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
