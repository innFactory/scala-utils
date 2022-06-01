package de.innfactory.play.flyway

import java.util.Properties
import org.flywaydb.core.internal.jdbc.DriverDataSource
import play.api.{Configuration, Environment, Logger}

/** Migrate Flyway on application start */
class FlywayMigrator (configuration: Configuration, env: Environment, configIdentifier: String, migrationsFilePath: String = "conf/db/migration") {
  private val logger   = Logger("application")

  logger.info("Creating Flyway context")

  private val driver   = configuration.get[String](s"$configIdentifier.driver")
  private val url      = configuration.get[String](s"$configIdentifier.url")
  private val user     = configuration.get[String](s"$configIdentifier.user")
  private val password = configuration.get[String](s"$configIdentifier.password")

  import org.flywaydb.core.Flyway

  val flyway: Flyway = Flyway.configure
    .dataSource(new DriverDataSource(env.classLoader, driver, url, user, password))
    .schemas("postgis")
    .baselineOnMigrate(true)
    .locations(s"filesystem:$migrationsFilePath")
    .load
  logger.info("Flyway is migrating the database to the newest version")
  flyway.migrate()
  logger.info("Database migration complete")
}
