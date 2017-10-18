name := "asynccom"

organization := "com.kindone"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.3"


lazy val root = project.in(file(".")).
  aggregate(crossJS, crossJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val crosslib = crossProject.in(file(".")).
  settings(
    name := "asynccom",
    organization := "com.kindone",
    version := "0.1-SNAPSHOT"
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.kindone" %% "event" % "0.1-SNAPSHOT",
      "com.kindone" %% "timer" % "0.1-SNAPSHOT"
    )
  ).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.kindone" %%% "event" % "0.1-SNAPSHOT",
      "com.kindone" %%% "timer" % "0.1-SNAPSHOT"
    )
  )

lazy val crossJVM = crosslib.jvm
lazy val crossJS = crosslib.js
