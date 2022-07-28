ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """backend-exercise-api""",
    libraryDependencies ++= Seq(
      guice,
      "com.auth0" % "java-jwt" % "3.3.0",
      "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
      "org.projectlombok" % "lombok" % "1.18.12",
      "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0"
    )
  )