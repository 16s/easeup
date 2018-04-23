import sbt._

object Dependencies {
  object Versions {
    val shapeless = "2.3.3"
    val cats = "1.1.0"
    val circe = "0.9.3"
    val http4s = "0.18.9"
  }

  val commonDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )

  val core = commonDependencies ++ Seq(
    "com.chuusai" %% "shapeless" % Versions.shapeless,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "org.typelevel" %% "cats-kernel" % Versions.cats,
    "org.typelevel" %% "cats-macros" % Versions.cats,
    "org.typelevel" %% "cats-effect" % "0.10",
    "net.16shells" %% "result" % "0.0.2"
  )

  val circe = commonDependencies ++ Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  val http4s = commonDependencies ++ Seq(
    "org.http4s" %% "http4s-dsl" % Versions.http4s,
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
    "org.http4s" %% "http4s-blaze-client" % Versions.http4s
  )
}
