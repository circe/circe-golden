ThisBuild / tlBaseVersion := "0.5"
ThisBuild / description := "Circe Golden Testing"
ThisBuild / circeRootOfCodeCoverage := None
ThisBuild / startYear := Some(2016)

val scala212 = "2.12.20"
val scala213 = "2.13.16"
val scala3 = "3.3.3"
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala212, scala213, scala3)

val circeVersion = "0.14.9"
val scalacheckVersion = "1.18.1"
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
    tlVersionIntroduced := List("2.13", "2.12", "3").map(_ -> "0.5.0").toMap
  )

lazy val example1 = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("examples/example-1"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "org.typelevel" %%% "discipline-scalatest" % disciplineScalatestVersion % Test
    )
  )
  .enablePlugins(NoPublishPlugin)
  .dependsOn(golden % Test)

ThisBuild / developers := List(
  tlGitHubDev(
    "travisbrown",
    "Travis Brown"
  ),
  tlGitHubDev(
    "zarthross",
    "Darren Gibson"
  ),
  tlGitHubDev(
    "hamnis",
    "Erlend Hamnaberg"
  )
)
