
//ThisBuild / scalaVersion := "2.13.8"
scalaVersion := "2.12.9"
ThisBuild / version := "1.0-SNAPSHOT"

val akkaManagementVersion = "1.0.0"
val akkaVersion = "2.6.19"
val akkaHTTPVersion = "10.1.15"

//val akkaManagementVersion = "1.0.0"
////val akkaVersion = "2.6.0"
//val akkaVersion = "2.6.19"
//val akkaHTTPVersion = "10.1.10"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """backend-exercise-api""",
    libraryDependencies ++= Seq(
      guice,
      "com.auth0" % "java-jwt" % "3.3.0",
      "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
      "org.projectlombok" % "lombok" % "1.18.12",
      "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
      "org.mindrot" % "jbcrypt" % "0.3m",
      "org.glassfish" % "javax.el" % "3.0.1-b09",
      "org.hibernate.validator" % "hibernate-validator" % "6.1.5.Final",
      "org.hibernate.validator" % "hibernate-validator-cdi" % "6.1.5.Final",
      // akka related stuff
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
      "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      // akka cluster related stuff
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
      // akka http related stuff
      "com.typesafe.akka" %% "akka-http-core" % akkaHTTPVersion,
      "com.typesafe.akka" %% "akka-http2-support" % akkaHTTPVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHTTPVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHTTPVersion
    )
  )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"