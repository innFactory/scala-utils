package de.innfactory.play.db.codegen

abstract class Config {
  lazy val databaseHost: String = sys.env.getOrElse("DATABASE_HOST", "localhost")
  lazy val databasePort: String = sys.env.getOrElse("DATABASE_PORT", "5432")
  lazy val databaseDb: String = sys.env.getOrElse("DATABASE_DB", "test")
  lazy val databaseUser: String = sys.env.getOrElse("DATABASE_USER", "test")
  lazy val databasePassword: String = sys.env.getOrElse("DATABASE_PASSWORD", "test")
  lazy val databaseUrl: String =
    s"jdbc:postgresql://$databaseHost:$databasePort/$databaseDb?user=$databaseUser&password=$databasePassword"
  lazy val url: String = databaseUrl
  lazy val jdbcDriver: String = "org.postgresql.Driver"
  lazy val slickProfile: XPostgresProfile = XPostgresProfile
}
