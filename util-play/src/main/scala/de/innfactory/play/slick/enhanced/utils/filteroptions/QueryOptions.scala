package de.innfactory.play.slick.enhanced.utils.filteroptions

case class MinOptionInt(value: Option[Int])
case class MaxOptionInt(value: Option[Int])

case class MinOptionDouble(value: Option[Double])
case class MaxOptionDouble(value: Option[Double])

case class EqualsOptionBoolean(value: Option[Boolean])

case class MinOptionLong(value: Option[Long])
case class MaxOptionLong(value: Option[Long])

case class StartsWithOptionString(value: Option[String])
case class EndsWithOptionString(value: Option[String])
case class IncludesOptionString(value: Option[String])
case class NotIncludesOptionString(value: Option[String])
case class EqualsOptionString(value: Option[String])
case class NotEqualsOptionString(value: Option[String])
