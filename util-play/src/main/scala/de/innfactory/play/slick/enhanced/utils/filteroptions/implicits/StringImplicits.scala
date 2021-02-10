package de.innfactory.play.slick.enhanced.utils.filteroptions.implicits

import scala.util.Try

object StringImplicits {
  implicit class EnhancedStringOption(value: Option[String]) {
    def toInt: Option[Int]       =
      value.flatMap(s => Try(s.toInt).toOption)
    def toLong: Option[Long]     =
      value.flatMap(s => Try(s.toLong).toOption)
    def toDouble: Option[Double] =
      value.flatMap(s => Try(s.toDouble).toOption)
  }
}
