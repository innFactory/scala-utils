package de.innfactory.play.slick.enhanced.utils.filteroptions

import slick.lifted.Rep

abstract class BooleanFilterOptions[E] extends OptionalBooleanFilterOptions[E, Boolean]

abstract class OptionalBooleanFilterOptions[E, V] extends FilterOptions[E, V] {

  override def selector: E => Rep[V]

  override def optionsToString: String =
    s"equalsOption=$equalsOption | "

  override def atLeasOneFilterOptionApplicable: Boolean =
    equalsOption.value.isDefined

  def withOptions(
    equalsOptionBoolean: EqualsOptionBoolean
  ): OptionalBooleanFilterOptions[E, V] = {
    equalsOption = equalsOptionBoolean
    this
  }

  def equalsQueryParam: String = queryIdentifier + "Equals"

  var equalsOption: EqualsOptionBoolean = EqualsOptionBoolean(None)

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[OptionalBooleanFilterOptions[E, V]] =
    try {
      params.get(equalsQueryParam) match {
        case Some(value) if value.nonEmpty => equalsOption = EqualsOptionBoolean(Some(value.headOption.get.toBoolean))
        case None                          => equalsOption = EqualsOptionBoolean(None)
      }
      if (equalsOption.value.isDefined)
        Some(
          withOptions(
            equalsOption
          )
        )
      else None
    } catch {
      case _: Exception => None
    }

}
