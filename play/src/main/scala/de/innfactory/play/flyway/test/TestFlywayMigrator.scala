package de.innfactory.play.flyway.test

import java.util.Properties

import org.flywaydb.core.internal.jdbc.DriverDataSource
import play.api.{Configuration, Environment, Logger}

abstract class TestFlywayMigrator(configuration: Configuration, env: Environment, configIdentifier: String = "test", migrationsFilePath: String = "conf/db/migration", testMigrationsFilePath: String = "test/resources/migration") {
  private val logger = Logger("application")
  def onStart(): Unit = {

    logger.info("Creating Flyway Test Context")

    val driver   = configuration.get[String](s"$configIdentifier.database.driver")
    val url      = configuration.get[String](s"$configIdentifier.database.testUrl")
    val user     = configuration.get[String](s"$configIdentifier.database.testUser")
    val password = configuration.get[String](s"$configIdentifier.database.testPassword")

    import org.flywaydb.core.Flyway

    val flyway: Flyway = Flyway.configure
      .dataSource(new DriverDataSource(env.classLoader, driver, url, user, password))
      .schemas("postgis")
      .baselineOnMigrate(true)
      .locations(s"filesystem:$migrationsFilePath", s"filesystem:$testMigrationsFilePath")
      .load
    logger.info("Cleaning Flyway Test Database")
    flyway.clean()
    logger.info("Flyway/Migrate")
    flyway.migrate()
    logger.info("Test MIGRATION FINISHED")
    logger.info(flyway.info().toString)
  }
  onStart()
}