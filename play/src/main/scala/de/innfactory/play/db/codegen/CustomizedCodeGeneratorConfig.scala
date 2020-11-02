package de.innfactory.play.db.codegen

case class CustomizedCodeGeneratorConfig(
  profile: String = "de.innfactory.play.db.codegen.XPostgresProfile",
  folder: String = "/target/scala-2.13/src_managed/slick",
  pkg: String = "dbdata",
  container: String = "Tables",
  fileName: String = "Tables.scala"
)
