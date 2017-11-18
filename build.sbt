lazy val projectName = "asynccom"

lazy val projectOrganization = "com.kindone"

lazy val projectVersion = "0.1-SNAPSHOT"

name := projectName

organization := projectOrganization

version := projectVersion

scalaVersion := "2.12.3"


lazy val root = project.in(file(".")).
  aggregate(crossJS, crossJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val crosslib = crossProject.in(file(".")).
  settings(
    name := projectName,
    organization := projectOrganization,
    version := projectVersion
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.kindone" %% "event" % "0.1-SNAPSHOT",
      "com.kindone" %% "timer" % "0.1-SNAPSHOT",
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
    )
  ).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.kindone" %%% "event" % "0.1-SNAPSHOT",
      "com.kindone" %%% "timer" % "0.1-SNAPSHOT",
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "org.scalatest" %%% "scalatest" % "3.0.4" % "test",
      "org.scalamock" %%% "scalamock-scalatest-support" % "3.6.0" % "test"
    ),
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )

lazy val crossJVM = crosslib.jvm
lazy val crossJS = crosslib.js
