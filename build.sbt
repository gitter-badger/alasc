// inspired by Spire build.sbt file

val scala210Version = "2.10.6"
val scala211Version = "2.11.8"
val scala212Version = "2.12.1"

val attributesVersion = "0.30"
val disciplineVersion = "0.7.2"
val cycloVersion = "0.14.1.0"
val metalVersion = "0.14.1.0"
val scalaCheckVersion = "1.13.4"
val scalaTestVersion = "3.0.1"
val scalinVersion = "0.14.1.0"
val shapelessVersion = "2.3.2"
val spireVersion = "0.14.1"
val fastParseVersion = "0.4.2"

lazy val alasc = (project in file("."))
  .settings(moduleName := "alasc")
  .settings(alascSettings: _*)
  .settings(noPublishSettings)
  .aggregate(core, gap3, laws, tests)
  .dependsOn(core, gap3, laws, tests)

lazy val core = (project in file("core"))
  .settings(moduleName := "alasc-core")
  .settings(alascSettings: _*)
  .settings(commonJvmSettings: _*)

lazy val gap3 = (project in file("gap3"))
  .settings(moduleName := "alasc-gap3")
  .settings(alascSettings: _*)
  .settings(commonJvmSettings: _*)
    .settings(initialCommands in console :=
      """
        |import net.alasc.perms._
        |import net.alasc.finite._
        |import net.alasc.perms.default._
        |import spire.implicits._
        |import net.alasc.syntax.all._
        |import net.alasc.enum._
        |import net.alasc.domains._
        |import net.alasc.std.any._
        |import net.alasc.wreath._
        |import net.alasc.algebra._
        |import scalin.syntax.all._
        |import scalin.immutable.dense._
        |import spire.math._
        |import net.alasc.finite.Rep.syntax._
        |import scalin.immutable.{Mat,Vec}
        |import net.alasc.gap3._
        |import cyclo.Cyclo
        |import net.alasc.print._
        |import CGLMP3._
      """.stripMargin)
  .dependsOn(core, laws)

lazy val laws = (project in file("laws"))
  .settings(moduleName := "alasc-laws")
  .settings(alascSettings: _*)
  .settings(testSettings:_*)
  .settings(commonJvmSettings: _*)
  .dependsOn(core)

lazy val tests = (project in file("tests"))
  .settings(moduleName := "alasc-tests")
  .settings(alascSettings: _*)
  .settings(testSettings:_*)
  .settings(noPublishSettings:_*)
  .settings(commonJvmSettings: _*)
  .dependsOn(core, gap3, laws)

lazy val alascSettings = buildSettings ++ commonSettings ++ publishSettings

lazy val buildSettings = Seq(
  organization := "net.alasc",
  scalaVersion := scala212Version,
  crossScalaVersions := Seq(scala211Version, scala210Version)
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.diff(Seq(
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )),
  resolvers ++= Seq(
    "bintray/denisrosset/maven" at "https://dl.bintray.com/denisrosset/maven",
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  libraryDependencies ++= Seq(
    "net.alasc" %% "attributes" % attributesVersion,
    "net.alasc" %% "spire-cyclo" % cycloVersion,
    "org.typelevel" %% "spire" % spireVersion,
    "org.typelevel" %% "spire-laws" % spireVersion,
    "net.alasc" %% "scalin-core" % scalinVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.scala-metal" %% "metal-core" % metalVersion,
    "org.scala-metal" %% "metal-library" % metalVersion,
    "com.lihaoyi" %% "fastparse" % fastParseVersion
  )    
) ++ scalaMacroDependencies ++ warnUnusedImport

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/denisrosset/unparsing")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  pomExtra := (
    <scm>
      <url>git@github.com:denisrosset/unparsing.git</url>
      <connection>scm:git:git@github.com:denisrosset/unparsing.git</connection>
    </scm>
    <developers>
      <developer>
        <id>denisrosset</id>
        <name>Denis Rosset</name>
        <url>http://github.com/denisrosset/</url>
      </developer>
    </developers>
  ),
  bintrayRepository := "maven",
  publishArtifact in Test := false
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
) ++ selectiveOptimize
  // -optimize has no effect in scala-js other than slowing down the build

// do not optimize on Scala 2.10 because of optimizer bugs (cargo-cult setting
// from my experience with metal)
lazy val selectiveOptimize = 
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq()
      case Some((2, 11)) => Seq("-optimize")
      case Some((2, 12)) => Seq()
      case _ => sys.error("Unknown Scala version")
    }
  }

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console))
)

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
      // in Scala 2.10, quasiquotes are provided by macro paradise
      case Some((2, 10)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
              "org.scalamacros" %% "quasiquotes" % "2.0.1" cross CrossVersion.binary
        )
    }
  }
)

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion,
    "org.typelevel" %% "discipline" % disciplineVersion,
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  )
)
