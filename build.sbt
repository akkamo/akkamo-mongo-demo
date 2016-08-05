name := "akkamo-mongo-demo"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "eu.akkamo" %% "akkamo" % "1.0.2-SNAPSHOT",
  "eu.akkamo" %% "akkamo-mongo" % "1.0.2-SNAPSHOT"
)

lazy val root = project.in(file(".")).enablePlugins(AkkamoSbtPlugin)
