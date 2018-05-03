import scalariform.formatter.preferences._

lazy val commonSettings = Seq(
	organization := "me.16s",
	scalaVersion := "2.12.6",
	version := "0.0.1-SNAPSHOT",
	wartremoverErrors ++= Warts.all,
  scalariformPreferences := scalariformPreferences.value,
  resolvers ++= Seq(
		Resolver.sonatypeRepo("releases"),
		Resolver.sonatypeRepo("snapshots")
	),
  scalacOptions ++= Seq("-Ypartial-unification", "-feature", "-deprecation", "-unchecked")
)

lazy val easeUpCore = (project in file("core"))
	.settings(
		commonSettings,
		name := "easeup-core",
    libraryDependencies ++= Dependencies.core
	)

lazy val easeUpCirce = (project in file("circe"))
	.dependsOn(easeUpCore)
	.settings(
		commonSettings,
		name := "easeup-circe",
    libraryDependencies ++= Dependencies.circe
	)

lazy val easeUpHttp4s = (project in file("http4s"))
	.dependsOn(easeUpCore)
	.settings(
		commonSettings,
		name := "easeup-http4s",
    libraryDependencies ++= Dependencies.http4s
	)

lazy val easeUp = (project in file("."))
  .settings(
		commonSettings,
		name := "easeup"
	).aggregate(easeUpCore, easeUpCirce, easeUpHttp4s)
