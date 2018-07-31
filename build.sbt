name := "delphi-webapi"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.4"

libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.12"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"

val elastic4sVersion = "6.2.8"
libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,

  // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,

  // if you want to use reactive streams
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion,

  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % "test"
)


lazy val webapi = (project in file(".")).
  enablePlugins(JavaAppPackaging).
  enablePlugins(DockerPlugin).
  enablePlugins(ScalastylePlugin).
  settings (
    dockerBaseImage := "openjdk:jre-alpine"
  ).
  enablePlugins(AshScriptPlugin).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "de.upb.cs.swt.delphi.webapi"
  )

lazy val scalastyleTask = taskKey[Unit]("scalastyleTask")
scalastyleTask :={
  scalastyle.in(Compile).toTask("").value
  scalastyle.in(Test).toTask("").value
}
(scalastyleConfig in Compile):=file("project/scalastyle-config.xml")
(scalastyleConfig in Test):=file("project/scalastyle-config.xml")
(test in Test) := ((test in Test) dependsOn scalastyleTask).value
