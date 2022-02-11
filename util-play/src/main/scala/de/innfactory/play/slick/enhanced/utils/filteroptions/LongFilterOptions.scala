package de.innfactory.play.slick.enhanced.utils.filteroptions

import de.innfactory.play.slick.enhanced.utils.filteroptions.implicits.StringImplicits.EnhancedStringOption
import slick.lifted.Rep

abstract class LongFilterOptions[E] extends FilterOptions[E, Long] {

  override def selector: E => Rep[Long]

  override def optionsToString: String =
    s"minOption=$minOption | " +
      s"maxOption=$maxOption |"

  override def atLeasOneFilterOptionApplicable: Boolean =
    minOption.value.isDefined || maxOption.value.isDefined

  var minOption: MinOptionLong = MinOptionLong(None)
  var maxOption: MaxOptionLong = MaxOptionLong(None)

  def minQueryParam: String = queryIdentifier + "Min"

  def maxQueryParam: String = queryIdentifier + "Max"

  def withOptions(
    minOptionLong: MinOptionLong,
    maxOptionLong: MaxOptionLong
  ): LongFilterOptions[E] = {
    minOption = minOptionLong
    maxOption = maxOptionLong
    this
  }

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[LongFilterOptions[E]] =
    try {
      params.get(minQueryParam) match {
        case Some(value) => minOption = MinOptionLong(value.headOption.toLong)
        case None        => minOption = MinOptionLong(None)
      }
      params.get(maxQueryParam) match {
        case Some(value) => maxOption = MaxOptionLong(value.headOption.toLong)
        case None        => maxOption = MaxOptionLong(None)
      }
      Some(
        withOptions(
          minOption,
          maxOption
        )
      )
    } catch {
      case _: Exception => None
    }

}
