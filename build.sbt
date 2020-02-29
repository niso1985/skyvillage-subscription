name := """skyvillage-subscription"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.stripe" % "stripe-java" % "10.0.2",
  "com.typesafe" % "config" % "1.4.0",
  "org.postgresql" % "postgresql" % "42.2.10",
  "org.flywaydb" %% "flyway-play" % "3.2.0",
  ws
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )
