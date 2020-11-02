import com.typesafe.config.ConfigFactory
import sbt.{ Def, _ }

//settings

name := """scala-utils"""
organization := "de.innfactory"
val releaseVersion = "1.0.69"

val defaultProjectSettings = Seq(
  scalaVersion := "2.13.3",
  organization := "de.innfactory",
  version := releaseVersion,
)



val sharedSettings = defaultProjectSettings

lazy val utilAuth = (project in file("util-auth")
).settings(
  sharedSettings
).settings(
  name := "util-auth",
  libraryDependencies ++= Seq(
    "com.google.firebase" % "firebase-admin" % "7.0.1",
    "com.nimbusds" % "nimbus-jose-jwt" % "9.0.1"
  )
)

lazy val utilGeo = (project in file("util-geo")
  ).settings(
  sharedSettings
).settings(
  name := "util-geo",
  libraryDependencies ++= Seq(slickPgJts)
)

lazy val utilGraphQL = Project(
  id = "util-graphql",
  base = file("util-graphql")
).settings(
  sharedSettings
).settings(
  name := "util-graphql",
  libraryDependencies ++= Seq(
    typesafePlay,
    playJson,
    "org.sangria-graphql" %% "sangria" % "2.0.0",
    "org.sangria-graphql" %% "sangria-slowlog" % "2.0.0-M1"
  )
)

lazy val utilImplicits = Project(
  id = "util-implicits",
  base = file("util-implicits")
).settings(
  sharedSettings
).settings(
  name := "util-implicits",
  libraryDependencies ++= Seq(
    typesafePlay,
    playJson,
  )
)


val slick           = "com.typesafe.slick"   %% "slick"              % "3.3.3"
val slickCodegen    = "com.typesafe.slick"   %% "slick-codegen"      % "3.3.3"
val slickHikaricp   = "com.typesafe.slick"   %% "slick-hikaricp"     % "3.3.3"
val hikariCP        = "com.zaxxer"            % "HikariCP"           % "3.4.5"
val slickPg         = "com.github.tminglei"  %% "slick-pg"           % "0.19.3"
val slickPgPlayJson = "com.github.tminglei"  %% "slick-pg_play-json" % "0.19.3"
val slickPgJts      = "com.github.tminglei"  %% "slick-pg_jts"       % "0.19.3"
val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper"  % "2.4.2"
val flyWayCore      = "org.flywaydb"          % "flyway-core"        % "6.5.7"

val typesafePlay = "com.typesafe.play" %% "play" % "2.8.3"
val playJson = "com.typesafe.play" %% "play-json" % "2.9.1"

lazy val play = Project(
  id = "play",
  base = file("play")
).settings(
  sharedSettings
).settings(
  libraryDependencies ++= Seq(
    playJson,
    typesafePlay,
    slickPg,
    slickPgPlayJson,
    slickPgJts,
    slickJodaMapper,
    slick,
    slickCodegen,
    slickHikaricp,
    hikariCP,
    flyWayCore
  )
)


lazy val root = project.in(file(".")).settings(sharedSettings).dependsOn(play, utilAuth, utilGeo, utilGraphQL).aggregate(play, utilAuth, utilGeo, utilGraphQL)
