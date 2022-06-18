import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{ buildInfoKeys, buildInfoOptions, buildInfoPackage }
import sbtbuildinfo.{ BuildInfoKey, BuildInfoOption }

import Dependencies._
import scala.util.Try
import sbt._
import Keys._

ThisBuild / organization := "com.fayimora.sadp"
ThisBuild / scalaVersion := "3.1.2"

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-explain",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
    "-Ykind-projector",
    "-Ysafe-init", // experimental (I've seen it cause issues with circe)
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future-migration")

lazy val `scala-app-deployment-playground` =
  project
    .settings(
      Compile / mainClass := Some("com.fayimora.sadp.Main")
    )
    .in(file("."))
    .enablePlugins(DockerPlugin)
    .enablePlugins(JavaServerAppPackaging)
    .settings(name := "scala-app-deployment-playground")
    .settings(commonSettings)
    .settings(dependencies)
    .settings(fatJarSettings)
    .settings(dockerSettings)

lazy val commonSettings = commonScalacOptions ++ Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty
)

lazy val commonScalacOptions = Seq(
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings",
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val dockerSettings = Seq(
  dockerExposedPorts := Seq(8090),
  dockerBaseImage := "openjdk:11",
  Docker / packageName := "zio-hello-world",
  dockerUsername := Some("fayi"),
  dockerUpdateLatest := true,
  // dockerRepository := sys.env.get("ECR_REPO"),
  Docker / publishLocal := (Docker / publishLocal).value,
  Docker / version := s"${version.value}"
  // Docker / version := s"${version.value}-${git
  //   .gitDescribedVersion
  //   .value
  //   .getOrElse(git.formattedShaVersion.value.getOrElse("latest"))}",
  // git.uncommittedSignifier := Some("dirty"),
  // ThisBuild / git.formattedShaVersion := {
  //   val base = git.baseVersion.?.value
  //   val suffix = git.makeUncommittedSignifierSuffix(
  //     git.gitUncommittedChanges.value,
  //     git.uncommittedSignifier.value,
  //   )
  //   git.gitHeadCommit.value.map { sha =>
  //     git.defaultFormatShaVersion(base, sha.take(7), suffix)
  //   }
  // },
)

lazy val fatJarSettings = Seq(
  assembly / assemblyJarName := "scala-app-deployment-playground.jar",
  assembly / assemblyMergeStrategy := {
    // case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties"       => MergeStrategy.first
    // case PathList(ps @ _*) if ps.last endsWith "pom.properties"                     => MergeStrategy.first
    // case PathList(ps @ _*) if ps.last endsWith "scala-collection-compat.properties" => MergeStrategy.first
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  },
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    action("lastCommitHash") {
      import scala.sys.process._
      // if the build is done outside of a git repository, we still want it to succeed
      Try("git rev-parse HEAD".!!.trim).getOrElse("?")
    },
  ),
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.ToMap,
  buildInfoPackage := "com.fayimora.sadp.version",
  buildInfoObject := "BuildInfo",
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main dependencies
    "dev.zio" %% "zio" % "2.0.0-RC6",
    "io.d11" %% "zhttp" % "2.0.0-RC9",
  ),
  libraryDependencies ++= Seq(
    org.scalatest.scalatest,
    org.scalatestplus.`scalacheck-1-16`,
  ).map(_ % Test),
)
