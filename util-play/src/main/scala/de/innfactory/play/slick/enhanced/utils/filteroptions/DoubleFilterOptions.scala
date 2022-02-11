package de.innfactory.play.slick.enhanced.utils.filteroptions


import de.innfactory.play.slick.enhanced.utils.filteroptions.implicits.StringImplicits.EnhancedStringOption
import slick.lifted.Rep

abstract class DoubleFilterOptions[E] extends FilterOptions[E, Double] {

  override def selector: E => Rep[Double]

  override def optionsToString: String =
    s"minOption=$minOption | " +
      s"maxOption=$maxOption |"

  override def atLeasOneFilterOptionApplicable: Boolean =
    minOption.value.isDefined || maxOption.value.isDefined

  var minOption: MinOptionDouble = MinOptionDouble(None)
  var maxOption: MaxOptionDouble = MaxOptionDouble(None)

  def minQueryParam: String = queryIdentifier + "Min"
  def maxQueryParam: String = queryIdentifier + "Max"

  def withOptions(
    minOptionDouble: MinOptionDouble,
    maxOptionDouble: MaxOptionDouble
  ): DoubleFilterOptions[E] = {
    minOption = minOptionDouble
    maxOption = maxOptionDouble
    this
  }

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[DoubleFilterOptions[E]] =
    try {
      params.get(minQueryParam) match {
        case Some(value) => minOption = MinOptionDouble(value.headOption.toDouble)
        case None        => minOption = MinOptionDouble(None)
      }
      params.get(maxQueryParam) match {
        case Some(value) => maxOption = MaxOptionDouble(value.headOption.toDouble)
        case None        => maxOption = MaxOptionDouble(None)
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
