import sbt._
//settings

name := """scala-utils"""
val releaseVersion = "2.0.0"

val token = sys.env.getOrElse("GITHUB_TOKEN", "")

val githubSettings = Seq(
  githubOwner := "innFactory",
  githubRepository := "de.innfactory.scala-utils",
  githubRepository := "scala-utils",
  githubTokenSource := TokenSource.GitConfig("github.token") || TokenSource.Environment("GITHUB_TOKEN"),
  credentials :=
    Seq(
      Credentials(
        "GitHub Package Registry",
        "maven.pkg.github.com",
        "innFactory",
        token
      )
    )
)

val defaultProjectSettings = Seq(
  scalaVersion := "2.13.8",
  organization := "de.innfactory.scala-utils",
  version := releaseVersion,
  githubOwner := "innFactory"
) ++ githubSettings

val sharedSettings = defaultProjectSettings

val sangria               = "org.sangria-graphql" %% "sangria"                 % "2.0.0"
val sangriaMarshallingApi = "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.4"
val sangriaSlowlog        = "org.sangria-graphql" %% "sangria-slowlog"         % "2.0.0-M1"

lazy val utilGraphQL = (project in file("util-graphql"))
  .settings(
    sharedSettings
  )
  .settings(
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

lazy val utilImplicits = (project in file("util-implicits")).settings(
  sharedSettings
) settings (
  name := "util-implicits"
)
val slickPgJts         = "com.github.tminglei"  %% "slick-pg_jts"       % "0.19.3"
val slick              = "com.typesafe.slick"   %% "slick"              % "3.3.3"
val slickCodegen       = "com.typesafe.slick"   %% "slick-codegen"      % "3.3.3"
val slickHikaricp      = "com.typesafe.slick"   %% "slick-hikaricp"     % "3.3.3"
val hikariCP           = "com.zaxxer"            % "HikariCP"           % "5.0.1"
val slickPg            = "com.github.tminglei"  %% "slick-pg"           % "0.20.2"
val slickPgPlayJson    = "com.github.tminglei"  %% "slick-pg_play-json" % "0.20.2"
val slickJodaMapper    = "com.github.tototoshi" %% "slick-joda-mapper"  % "2.4.2"
val flyWayCore         = "org.flywaydb"          % "flyway-core"        % "8.4.1"
val joda               = "joda-time"             % "joda-time"          % "2.10.13"

val playVersion  = "2.8.13"
val typesafePlay = "com.typesafe.play" %% "play"      % playVersion
val playWs       = "com.typesafe.play" %% "play-ws"   % playVersion
val playJson     = "com.typesafe.play" %% "play-json" % "2.9.2"

val opentelemetryApi           = "io.opentelemetry"               % "opentelemetry-api" % "1.18.0"
val opentelemetryBom           = "io.opentelemetry"               % "opentelemetry-bom" % "1.18.0"
val opentelemetrySdk           = "io.opentelemetry"               % "opentelemetry-sdk" % "1.18.0"
val opentelemetryGCloudTraces  = "com.google.cloud.opentelemetry" % "exporter-trace"    % "0.23.0"
val opentelemetryGCloudMetrics = "com.google.cloud.opentelemetry" % "exporter-metrics"  % "0.23.0"

val cats = "org.typelevel" %% "cats-core" % "2.7.0"

val googleCloudLogger  = "com.google.cloud" % "google-cloud-logging-logback" % "0.120.0-alpha"
val googleCloudLogging = "com.google.cloud" % "google-cloud-logging"         % "3.5.1"
val javaxactiviation   = "javax.activation" % "activation"                   % "1.1.1"

val sl4j        = "org.slf4j"        % "slf4j-api"                        % "1.7.32"
val sharedDeps  = "com.google.cloud" % "google-cloud-shared-dependencies" % "2.5.1"
val logback     = "ch.qos.logback"   % "logback-classic"                  % "1.2.10"
val logbackCore = "ch.qos.logback"   % "logback-core"                     % "1.2.10"

val endpointBuilder = "de.innfactory" %% "smithy4play" % "0.2.2-HOTFIX-4"

val guice = "com.google.inject" % "guice" % "4.2.3"

lazy val play = (project in file("util-play"))
  .settings(
    sharedSettings
  )
  .settings(
    name := "play",
    libraryDependencies ++= Seq(
      googleCloudLogger,
      googleCloudLogging,
      javaxactiviation,
      sl4j,
      sharedDeps,
      logback,
      logbackCore,
      cats,
      playJson,
      typesafePlay,
      slickPg,
      slickPgPlayJson,
      slickJodaMapper,
      slick,
      slickCodegen,
      slickHikaricp,
      hikariCP,
      flyWayCore,
      guice,
      playWs,
      endpointBuilder,
      slickPgJts,
      opentelemetryApi,
      opentelemetryBom,
      opentelemetrySdk,
      opentelemetryGCloudTraces,
      opentelemetryGCloudMetrics
    )
  )
  .dependsOn(utilImplicits)
  .aggregate(utilImplicits)

lazy val root = project
  .in(file("."))
  .settings(sharedSettings)
  .dependsOn(play, utilGraphQL, utilImplicits)
  .aggregate(play, utilGraphQL, utilImplicits)
