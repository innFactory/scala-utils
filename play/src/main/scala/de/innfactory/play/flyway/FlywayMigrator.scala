package de.innfactory.play.flyway

import java.util.Properties
import org.flywaydb.core.internal.jdbc.DriverDataSource
import javax.inject.Inject
import play.api.{Configuration, Environment, Logger}

/** Migrate Flyway on application start */
class FlywayMigrator (configuration: Configuration, env: Environment) {
  private val logger   = Logger("application")
  logger.info("Creating Flyway context")
  private val driver   = configuration.get[String]("bootstrap-play2.database.driver")
  private val url      = configuration.get[String]("bootstrap-play2.database.url")
  private val user     = configuration.get[String]("bootstrap-play2.database.user")
  private val password =
    configuration.get[String]("bootstrap-play2.database.password")

  import org.flywaydb.core.Flyway

  val flyway: Flyway = Flyway.configure
    .dataSource(new DriverDataSource(env.classLoader, driver, url, user, password, new Properties()))
    .schemas("postgis")
    .baselineOnMigrate(true)
    .locations("filesystem:conf/db/migration")
    .load
  logger.info("Flyway is migrating the database to the newest version")
  flyway.migrate()
  logger.info("Database migration complete")
}
