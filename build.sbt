import scalariform.formatter.preferences._

lazy val commonSettings = Seq(
	organization := "net.16shells",
	scalaVersion := "2.12.4",
	version := "0.0.1-SNAPSHOT",
	wartremoverErrors ++= Warts.all,
  scalariformPreferences := scalariformPreferences.value
)

lazy val easeUp = (project in file("."))
	.aggregate(easeUpCore, easeUpCirce, easeUpHttp4s)

lazy val easeUpCore = (project in file("core"))
	.settings(
		commonSettings,
    libraryDependencies ++= Dependencies.core
	)

lazy val easeUpCirce = (project in file("circe"))
	.dependsOn(easeUpCore)
	.settings(
		commonSettings,
    libraryDependencies ++= Dependencies.circe
	)

lazy val easeUpHttp4s = (project in file("http4s"))
	.dependsOn(easeUpCore)
	.settings(
		commonSettings,
    libraryDependencies ++= Dependencies.http4s
	)

