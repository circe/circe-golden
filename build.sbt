import sbtcrossproject.{ CrossType, crossProject }

ThisBuild / tlBaseVersion := "0.14"
ThisBuild / circeRootOfCodeCoverage := None
ThisBuild / startYear := Some(2016)
ThisBuild / scalafixScalaBinaryVersion := "2.12"
ThisBuild / githubWorkflowBuildMatrixFailFast := Some(false)

ThisBuild / crossScalaVersions := Seq("2.12.15", "2.13.7")

val circeVersion = "0.14.1"

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }

val baseSettings = Seq(
  resolvers += "jitpack".at("https://jitpack.io"),
  Compile / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  Test / console / scalacOptions ~= {
    _.filterNot(Set("-Ywarn-unused-import", "-Ywarn-unused:imports"))
  },
  coverageHighlighting := true
)

val root = tlCrossRootProject.aggregate(golden, example1)

lazy val golden = crossProject(JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("golden"))
  .settings(baseSettings)
  .settings(
    moduleName := "circe-golden",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-testing" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "org.typelevel" %%% "discipline-scalatest" % "2.1.5" % Test,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided
    )
  )

lazy val goldenJVM = golden.jvm

lazy val example1 = project
  .in(file("examples/example-1"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "org.scalacheck" %% "scalacheck" % "1.15.4",
      "org.typelevel" %%% "discipline-scalatest" % "2.1.5" % Test
    )
  )
  .enablePlugins(NoPublishPlugin)
  .dependsOn(goldenJVM % Test)

ThisBuild / developers := List(
  Developer(
    "travisbrown",
    "Travis Brown",
    "travisrobertbrown@gmail.com",
    url("https://twitter.com/travisbrown")
  )
)
