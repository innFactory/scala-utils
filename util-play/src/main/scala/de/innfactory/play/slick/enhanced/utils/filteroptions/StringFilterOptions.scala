package de.innfactory.play.slick.enhanced.utils.filteroptions

import de.innfactory.play.slick.enhanced.utils._
import slick.lifted.Rep

abstract class OptionStringFilterOptions[E] extends StringOrOptionStringFilterOptions[E, Option[String]]

abstract class StringFilterOptions[E] extends StringOrOptionStringFilterOptions[E, String]

abstract class StringOrOptionStringFilterOptions[E, V] extends FilterOptions[E, V] {

  override def selector: E => Rep[V]

  override def optionsToString: String =
    s"startsWithOption=$startsWithOption | " +
      s"endsWithOption=$endsWithOption |" +
      s"equalsOption=$equalsOption |" +
      s"notEqualsOption=$notEqualsOption |" +
      s"includesOption=$includesOption |" +
      s"notIncludesOption=$notIncludesOption"

  override def atLeasOneFilterOptionApplicable: Boolean =
    startsWithOption.value.isDefined ||
      endsWithOption.value.isDefined ||
      equalsOption.value.isDefined ||
      notEqualsOption.value.isDefined ||
      includesOption.value.isDefined ||
      notIncludesOption.value.isDefined

  def withOptions(
    startsWithOptionString: StartsWithOptionString,
    endsWithOptionString: EndsWithOptionString,
    equalsOptionString: EqualsOptionString,
    notEqualsOptionString: NotEqualsOptionString,
    includesOptionString: IncludesOptionString,
    notIncludesOptionString: NotIncludesOptionString
  ): StringOrOptionStringFilterOptions[E, V] = {
    startsWithOption = startsWithOptionString
    endsWithOption = endsWithOptionString
    equalsOption = equalsOptionString
    notEqualsOption = notEqualsOptionString
    includesOption = includesOptionString
    notIncludesOption = notIncludesOptionString
    this
  }

  def startsWithQueryParam: String  = queryIdentifier + "StartsWith"
  def endsWithQueryParam: String    = queryIdentifier + "EndsWith"
  def equalsQueryParam: String      = queryIdentifier + "Equals"
  def notEqualsQueryParam: String   = queryIdentifier + "NotEquals"
  def includesQueryParam: String    = queryIdentifier + "Includes"
  def notIncludesQueryParam: String = queryIdentifier + "NotIncludes"

  var startsWithOption: StartsWithOptionString   = StartsWithOptionString(None)
  var endsWithOption: EndsWithOptionString       = EndsWithOptionString(None)
  var equalsOption: EqualsOptionString           = EqualsOptionString(None)
  var notEqualsOption: NotEqualsOptionString     = NotEqualsOptionString(None)
  var includesOption: IncludesOptionString       = IncludesOptionString(None)
  var notIncludesOption: NotIncludesOptionString = NotIncludesOptionString(None)

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[StringOrOptionStringFilterOptions[E, V]] =
    try {
      params.get(startsWithQueryParam) match {
        case Some(value) => startsWithOption = StartsWithOptionString(value.headOption)
        case None        => startsWithOption = StartsWithOptionString(None)
      }
      params.get(endsWithQueryParam) match {
        case Some(value) => endsWithOption = EndsWithOptionString(value.headOption)
        case None        => endsWithOption = EndsWithOptionString(None)
      }
      params.get(includesQueryParam) match {
        case Some(value) => includesOption = IncludesOptionString(value.headOption)
        case None        => includesOption = IncludesOptionString(None)
      }
      params.get(notIncludesQueryParam) match {
        case Some(value) => notIncludesOption = NotIncludesOptionString(value.headOption)
        case None        => notIncludesOption = NotIncludesOptionString(None)
      }
      params.get(equalsQueryParam) match {
        case Some(value) => equalsOption = EqualsOptionString(value.headOption)
        case None        => equalsOption = EqualsOptionString(None)
      }
      params.get(notEqualsQueryParam) match {
        case Some(value) => notEqualsOption = NotEqualsOptionString(value.headOption)
        case None        => notEqualsOption = NotEqualsOptionString(None)
      }
      Some(
        withOptions(
          startsWithOption,
          endsWithOption,
          equalsOption,
          notEqualsOption,
          includesOption,
          notIncludesOption
        )
      )
    } catch {
      case _: Exception => None
    }

}
