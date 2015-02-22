name := "lambda-days"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= List(
  "org.scalaz" %% "scalaz-core" % "7.0.7",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "commons-codec" % "commons-codec" % "1.4",
  "io.argonaut" %% "argonaut" % "6.0.4"
)