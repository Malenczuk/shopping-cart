import sbt._

object Library {

  object Version {
    val akka = "2.5.25"
    val cats = "2.0.0"
    val enumeratum = "1.5.13"
    val shapeless = "2.3.3"
    val scalaTest = "3.0.8"
  }

  val akka        = "com.typesafe.akka" %% "akka-actor"   % Version.akka
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  val cats        = "org.typelevel"     %% "cats-core"    % Version.cats
  val enumeratum  = "com.beachape"      %% "enumeratum"   % Version.enumeratum
  val shapeless   = "com.chuusai"       %% "shapeless"    % Version.shapeless
  val scalaTest   = "org.scalatest"     %% "scalatest"    % Version.scalaTest
}
