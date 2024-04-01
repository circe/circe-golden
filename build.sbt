ThisBuild / tlBaseVersion := "0.4"
ThisBuild / description := "Yet another Typesafe Config decoder"
ThisBuild / circeRootOfCodeCoverage := Some("golden")
ThisBuild / startYear := Some(2016)

val scala212 = "2.12.18"
val scala213 = "2.13.11"
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala212, scala213)

val circeVersion = "0.14.1"
val scalacheckVersion = "1.15.4"
val disciplineScalatestVersion = "2.1.5"

val root = tlCrossRootProject.aggregate(golden, example1)

lazy val golden = crossProject(JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("golden"))
  .settings(
    moduleName := "circe-golden",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-testing" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "org.typelevel" %%% "discipline-scalatest" % disciplineScalatestVersion % Test,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided
    )
  )

lazy val goldenJVM = golden.jvm

lazy val example1 = project
  .in(file("examples/example-1"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "org.typelevel" %%% "discipline-scalatest" % disciplineScalatestVersion % Test
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
  ),
  Developer("zarthross", "Darren Gibson", "zarthross@gmail.com", url("https://twitter.com/zarthross"))
)
