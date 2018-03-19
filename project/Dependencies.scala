import sbt._

object Dependencies {
  object Versions {
    val shapeless = "2.3.3"
    val cats = "1.0.1"
  }

  val commonDependencies = Seq(

  )

  val core = commonDependencies ++ Seq(
    "com.chuusai" %% "shapeless" % Versions.shapeless,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "org.typelevel" %% "cats-kernel" % Versions.cats,
    "org.typelevel" %% "cats-macros" % Versions.cats,
    "org.typelevel" %% "cats-effect" % "0.10"
  )

  val circe = commonDependencies

  val http4s = commonDependencies
}
