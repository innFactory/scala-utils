import com.typesafe.config.ConfigFactory
import sbt.{Def, _}
//settings

name := """scala-utils"""
val releaseVersion = "1.0.93"

val token = sys.env.getOrElse("GITHUB_TOKEN", "")

credentials :=
  Seq(Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    "innFactory",
    token
  ))


val defaultProjectSettings = Seq(
  scalaVersion := "2.13.3",
  organization := "de.innfactory.scala-utils",
  version := releaseVersion,
  githubOwner := "innFactory",
  githubRepository := "scala-utils"
)

val sharedSettings = defaultProjectSettings

val firebaseAdmin =  "com.google.firebase" % "firebase-admin" % "7.0.1"
val nimbusJoseJwt =   "com.nimbusds" % "nimbus-jose-jwt" % "9.0.1"

lazy val utilAuth = (project in file("util-auth")
).settings(
  sharedSettings
).settings(
  name := "auth",
  libraryDependencies ++= Seq(
    firebaseAdmin,
    nimbusJoseJwt,
    playJson,
    typesafePlay
  )
)

val slickPgJts      = "com.github.tminglei"  %% "slick-pg_jts"       % "0.19.3"

lazy val utilGeo = (project in file("util-geo")
  ).settings(
  sharedSettings
).settings(
  name := "geo",
  libraryDependencies ++= Seq(slickPgJts)
)

val sangria =    "org.sangria-graphql" %% "sangria" % "2.0.0"
val sangriaMarshallingApi =    "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.4",
val sangriaSlowlog =    "org.sangria-graphql" %% "sangria-slowlog" % "2.0.0-M1"

lazy val utilGraphQL = (project in file("util-graphql")
  ).settings(
  sharedSettings
).settings(
  name := "graphql",
  libraryDependencies ++= Seq(
    typesafePlay,
    playJson,
    joda,
    sangria,
    sangriaMarshallingApi,
    sangriaSlowlog
  )
)

lazy val utilImplicits = (project in file("util-implicits")
  ).settings(
  sharedSettings
)settings(
  name := "util-implicits"
)


val slick           = "com.typesafe.slick"   %% "slick"              % "3.3.3"
val slickCodegen    = "com.typesafe.slick"   %% "slick-codegen"      % "3.3.3"
val slickHikaricp   = "com.typesafe.slick"   %% "slick-hikaricp"     % "3.3.3"
val hikariCP        = "com.zaxxer"            % "HikariCP"           % "3.4.5"
val slickPg         = "com.github.tminglei"  %% "slick-pg"           % "0.19.3"
val slickPgPlayJson = "com.github.tminglei"  %% "slick-pg_play-json" % "0.19.3"
val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper"  % "2.4.2"
val flyWayCore      = "org.flywaydb"          % "flyway-core"        % "7.1.1"
val joda            = "joda-time"             % "joda-time"          % "2.10.6"

val typesafePlay = "com.typesafe.play" %% "play" % "2.8.3"
val playJson = "com.typesafe.play" %% "play-json" % "2.9.1"

lazy val play = (project in file("util-play")
  ).settings(
  sharedSettings
).settings(
  name := "play",
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


lazy val root = project.in(file(".")).settings(sharedSettings).dependsOn(play, utilAuth, utilGeo, utilGraphQL, utilImplicits).aggregate(play, utilAuth, utilGeo, utilGraphQL, utilImplicits)
