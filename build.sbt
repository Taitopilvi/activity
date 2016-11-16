name := "activity"
organization := "fi.taitopilvi"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.11.8"

publishMavenStyle := true

licenses := Seq("LGPL v2.1+" -> url("http://www.gnu.org/licenses/lgpl-2.1.txt"))

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.8.0",
  "org.json4s" %% "json4s-native" % "3.5.0")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
