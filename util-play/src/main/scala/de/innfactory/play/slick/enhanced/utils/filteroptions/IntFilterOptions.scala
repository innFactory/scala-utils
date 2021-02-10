package de.innfactory.play.slick.enhanced.utils.filteroptions

import de.innfactory.play.slick.enhanced.utils.filteroptions.implicits.StringImplicits.EnhancedStringOption
import slick.lifted.Rep

abstract class IntFilterOptions[E] extends FilterOptions[E, Int] {

  override def selector: E => Rep[Int]

  override def optionsToString: String =
    s"minOption=$minOption | " +
      s"maxOption=$maxOption |"

  override def atLeasOneFilterOptionApplicable: Boolean =
    minOption.value.isDefined || maxOption.value.isDefined

  var minOption: MinOptionInt = MinOptionInt(None)
  var maxOption: MaxOptionInt = MaxOptionInt(None)

  def minQueryParam: String = queryIdentifier + "Min"
  def maxQueryParam: String = queryIdentifier + "Max"

  def withOptions(
    minOptionInt: MinOptionInt,
    maxOptionInt: MaxOptionInt
  ): IntFilterOptions[E] = {
    minOption = minOptionInt
    maxOption = maxOptionInt
    this
  }

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[IntFilterOptions[E]] =
    try {
      params.get(minQueryParam) match {
        case Some(value) => minOption = MinOptionInt(value.headOption.toInt)
        case None        => minOption = MinOptionInt(None)
      }
      params.get(maxQueryParam) match {
        case Some(value) => maxOption = MaxOptionInt(value.headOption.toInt)
        case None        => maxOption = MaxOptionInt(None)
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
