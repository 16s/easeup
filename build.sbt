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
  scalacOptions ++= Seq(
		"-Ypartial-unification",
		"-feature",
    "-deprecation",
    "-unchecked",
    "-explaintypes",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xlint:adapted-args",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",            // Warn if a private member is unused.
    "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
  )
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
