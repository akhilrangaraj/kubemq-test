name := "kubemq-test"

version := "0.1"

scalaVersion := "2.13.3"


resolvers += "Maven Repo" at  "https://repo1.maven.org/maven2/"

libraryDependencies += "io.kubemq.sdk" % "kubemq-sdk-Java" % "1.0.3"
// https://mvnrepository.com/artifact/commons-cli/commons-cli
libraryDependencies += "commons-cli" % "commons-cli" % "1.4"

