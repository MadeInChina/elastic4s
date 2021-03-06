import com.typesafe.sbt.pgp.PgpKeys
import sbt._
import sbt.Keys._

object Build extends Build {

  val org = "com.sksamuel.elastic4s"

  val ScalaVersion = "2.11.7"
  val ScalatestVersion = "2.2.5"
  val MockitoVersion = "1.9.5"
  val JacksonVersion = "2.6.1"
  val Slf4jVersion = "1.7.12"
  val ScalaLoggingVersion = "2.1.2"
  val ElasticsearchVersion = "2.2.1"
  val Log4jVersion = "1.2.17"
  val CommonsIoVersion = "2.4"

  val rootSettings = Seq(
    organization := org,
    scalaVersion := ScalaVersion,
    crossScalaVersions := Seq("2.11.7", "2.10.5"),
    publishMavenStyle := true,
    resolvers += Resolver.mavenLocal,
    fork in Test := true,
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled"),
    publishArtifact in Test := false,
    parallelExecution in Test := false,
    sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    javacOptions := Seq("-source", "1.7", "-target", "1.7"),
    libraryDependencies ++= Seq(
      "org.elasticsearch" % "elasticsearch" % ElasticsearchVersion,
      "org.scalactic" %% "scalactic" % "2.2.5",
      "org.slf4j" % "slf4j-api" % Slf4jVersion,
      "commons-io" % "commons-io" % CommonsIoVersion % "test",
      "log4j" % "log4j" % Log4jVersion % "test",
      "org.slf4j" % "log4j-over-slf4j" % Slf4jVersion % "test",
      "org.mockito" % "mockito-all" % MockitoVersion % "test",
      "org.scalatest" %% "scalatest" % ScalatestVersion % "test"
    ),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/sksamuel/elastic4s</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:sksamuel/elastic4s.git</url>
          <connection>scm:git@github.com:sksamuel/elastic4s.git</connection>
        </scm>
        <developers>
          <developer>
            <id>sksamuel</id>
            <name>sksamuel</name>
            <url>http://github.com/sksamuel</url>
          </developer>
        </developers>
    }
  )

  lazy val root = Project("elastic4s", file("."))
    .settings(rootSettings: _*)
    .settings(publish := {})
    .settings(publishArtifact := false)
    .settings(name := "elastic4s")
    .aggregate(
      core,
      testkit,
      coreTests,
      examples,
      jackson,
      json4s,
      streams
    )

  lazy val core = Project("elastic4s-core", file("elastic4s-core"))
    .settings(rootSettings: _*)
    .settings(name := "elastic4s-core")

  lazy val testkit = Project("elastic4s-testkit", file("elastic4s-testkit"))
    .settings(rootSettings: _*)
    .settings(
      name := "elastic4s-testkit",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % ScalatestVersion,
        "org.elasticsearch.module" % "lang-groovy" % ElasticsearchVersion,
        "org.elasticsearch" % "elasticsearch" % ElasticsearchVersion classifier "tests"
      )
    )
    .dependsOn(core)

  lazy val coreTests = Project("elastic4s-core-tests", file("elastic4s-core-tests"))
    .settings(rootSettings: _*)
    .settings(name := "elastic4s-core-tests")
    .settings(
      libraryDependencies += "org.scalatest" %% "scalatest" % ScalatestVersion % "test",
      libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion % "test",
      libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion % "test",
      libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion % "test" exclude("org.scala-lang", "scala-library")
    )
    .dependsOn(core, testkit % "test")

  lazy val streams = Project("elastic4s-streams", file("elastic4s-streams"))
    .settings(rootSettings: _*)
    .settings(
      name := "elastic4s-streams",
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12",
      libraryDependencies += "org.reactivestreams" % "reactive-streams" % "1.0.0",
      libraryDependencies += "org.reactivestreams" % "reactive-streams-tck" % "1.0.0" % "test"
    ).dependsOn(core, testkit % "test", jackson % "test")

  lazy val jackson = Project("elastic4s-jackson", file("elastic4s-jackson"))
    .settings(rootSettings: _*)
    .settings(
      name := "elastic4s-jackson",
      libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
      libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
      libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion exclude("org.scala-lang", "scala-library"),
      libraryDependencies += "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % JacksonVersion
    ).dependsOn(core, testkit % "test")

  lazy val json4s = Project("elastic4s-json4s", file("elastic4s-json4s"))
    .settings(rootSettings: _*)
    .settings(
      name := "elastic4s-json4s",
      libraryDependencies += "org.json4s" %% "json4s-core" % "3.2.11",
      libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.11"
    ).dependsOn(core, testkit % "test")

  lazy val examples = Project("elastic4s-examples", file("elastic4s-examples"))
    .settings(rootSettings: _*)
    .settings(publish := {})
    .settings(name := "elastic4s-examples")
    .dependsOn(core, jackson, streams)
}
