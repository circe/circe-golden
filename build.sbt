ThisBuild / tlBaseVersion := "0.4"
ThisBuild / description := "Yet another Typesafe Config decoder"
ThisBuild / circeRootOfCodeCoverage := None
ThisBuild / startYear := Some(2016)

val scala212 = "2.12.19"
val scala213 = "2.13.14"
val scala3 = "3.3.3"
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala212, scala213, scala3)

val circeVersion = "0.14.9"
val scalacheckVersion = "1.18.0"
val disciplineScalatestVersion = "2.3.0"
val scalacheckScalaTestVersion = "3.2.19.0"

val root = tlCrossRootProject.aggregate(golden, example1)

lazy val golden = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("golden"))
  .settings(
    moduleName := "circe-golden",
    libraryDependencies ++= Seq(
      "org.scalatestplus" %%% "scalacheck-1-18" % scalacheckScalaTestVersion,
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-testing" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "org.typelevel" %%% "discipline-scalatest" % disciplineScalatestVersion % Test
    ),
    libraryDependencies ++= {
      if (tlIsScala3.value) Nil
      else
        Seq(
          scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided
        )
    },
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    tlVersionIntroduced := Map("2.13" -> "0.14.0", "2.12" -> "0.4.0", "3" -> "0.4.1")
  )

lazy val example1 = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples/example-1"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "org.typelevel" %%% "discipline-scalatest" % disciplineScalatestVersion % Test
    )
  )
  .enablePlugins(NoPublishPlugin)
  .dependsOn(golden % Test)

ThisBuild / developers := List(
  Developer(
    "travisbrown",
    "Travis Brown",
    "travisrobertbrown@gmail.com",
    url("https://twitter.com/travisbrown")
  ),
  Developer(
    "zarthross",
    "Darren Gibson",
    "zarthross@gmail.com",
    url("https://twitter.com/zarthross")
  )
)
