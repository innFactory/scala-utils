package de.innfactory.play.db.codegen

import com.github.tminglei.slickpg.ExPostgresProfile
import slick.codegen.SourceCodeGenerator
import slick.sql.SqlProfile.ColumnOption

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 *  This customizes the Slick code generator.
 */
abstract class CustomizedCodeGeneratorBase[T <: ExPostgresProfile](customizedCodeGeneratorConfig: CustomizedCodeGeneratorConfig, config: Config[T]) {
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * for filtering out desired tables
   * has to contain each table name as UPPERCASE
   * @return Seq[String]
   */
  def included: Seq[String]

  /**
   * Should not be overwritten, try overriding rawTypeMatcherExtension
   * Only override if baseTypes should be changed
   * @param typeName
   * @return
   */
  def rawTypeMatcherBase(typeName: String): Option[String] = {
    typeName match {
      case "hstore"                                      => Option("Map[String, String]")
      case "_text" | "text[]" | "_varchar" | "varchar[]" => Option("List[String]")
      case "_int8" | "int8[]"                            => Option("List[Long]")
      case "_int4" | "int4[]"                            => Option("List[Int]")
      case "_int2" | "int2[]"                            => Option("List[Short]")
      case s: String                                     => rawTypeMatcherExtension(s)
    }
  }

  /**
   * RawTypeMatcher for non standard types like geometry
   * override for more type matching
   * @param typeName
   * @return
   */
  def rawTypeMatcherExtension(typeName: String): Option[String] = {
    typeName match {
      case "geometry"                                    => Option("com.vividsolutions.jts.geom.Geometry")
      case _ => None
    }
  }

  /**
   * sql raw type mapper to override standard sql types to custom types
   * @param typeName
   * @return
   */
  def sqlTypeMapper(typeName: String, superRawType: String): String = {
    typeName match {
      case "java.sql.Date"      => "org.joda.time.LocalDate"
      case "java.sql.Time"      => "org.joda.time.LocalTime"
      case "java.sql.Timestamp" => "org.joda.time.DateTime"
      case _                    => superRawType
    }
  }

  val codeGenImports: String = {
    s"""
       import com.github.tototoshi.slick.PostgresJodaSupport._
       import org.joda.time.DateTime
       import com.vividsolutions.jts.geom.Point
       """
  }

  val projectDir: String = System.getProperty("user.dir")

  def main(args: Array[String]): Unit =
  // write the generated results to file
    Await.result(
      codegen.map(
        _.writeToFile(
          profile = customizedCodeGeneratorConfig.profile, // Using customized Codegen profile from config
          folder= s"$projectDir${customizedCodeGeneratorConfig.folder}" ,
          pkg = customizedCodeGeneratorConfig.pkg,
          container= customizedCodeGeneratorConfig.container,
          fileName = customizedCodeGeneratorConfig.fileName
        )
      ),
      20.seconds
    )

  val slickProfile = config.slickProfile

  val db = slickProfile.api.Database.forURL(config.url, driver = config.jdbcDriver)

  lazy val codegen: Future[SourceCodeGenerator] = db.run {
    config.slickProfile.defaultTables.map(_.filter(t => included contains t.name.name.toUpperCase))
      .flatMap(
        config.slickProfile
          .createModelBuilder(_, ignoreInvalidDefaults = false)
          .buildModel
      )
  }.map { model =>
    new slick.codegen.SourceCodeGenerator(model) {
      override def Table =
        new Table(_) { table =>
          override def Column =
            new Column(_) { column =>
              // customize db type -> scala type mapping, pls adjust it according to your environment
              override def rawType: String =
                model.options
                  .find(_.isInstanceOf[ColumnOption.SqlType])
                  .flatMap( tpe => rawTypeMatcherBase(tpe.asInstanceOf[ColumnOption.SqlType].typeName))
                  .getOrElse(sqlTypeMapper(model.tpe, super.rawType))
            }
        }

      // ensure to use customized postgres driver at `import profile.simple._`
      override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String =
        s"""
package ${pkg}
${codeGenImports}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object ${container} extends {
  val profile = ${profile}
} with ${container}
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: $profile
  import profile.api._
  ${indent(code)}
}
      """.trim()
    }
  }
}
