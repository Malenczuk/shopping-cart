name := "EShop"

version := "0.2"

scalaVersion := "2.13.1"


libraryDependencies ++= Seq(
  Library.akka,
  Library.cats,
  Library.enumeratum,
  Library.shapeless,

  Library.akkaTestKit % Test,
  Library.scalaTest   % Test,
)


// scalaFmt
scalafmtOnCompile := true
