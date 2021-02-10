package de.innfactory.play.slick.enhanced.utils.filteroptions

import de.innfactory.play.db.codegen.XPostgresProfile

class FilterOptionUsageUtils[A, T <: XPostgresProfile.Table[A]](filterOptionsConfig: Seq[FilterOptions[A, _]]) {

  private def queryStringToOptionsSequence(implicit
    queryString: Map[String, Seq[String]]
  ): Seq[FilterOptions[A, _]] = {
    filterOptionsConfig
      .map(_.getFromQueryString(queryString))
      .filter(_.isDefined)
      .map(_.get)
      .filter(_.atLeasOneFilterOptionApplicable)
  }

  /**
   * Map Query String to Filter Options
   * @param queryString
   */
  def queryStringToFilterOptions(implicit
    queryString: Map[String, Seq[String]]
  ): Seq[FilterOptions[A, _]] = queryStringToOptionsSequence(queryString)

  def optionStringToFilterOptions(implicit
    optionString: Option[String]
  ): Seq[FilterOptions[A, _]] =
    optionString match {
      case Some(value) if !value.isBlank =>
        val query: Map[String, Seq[String]] = value
          .split('&')
          .map(_.split('='))
          .map(array => array.head -> array.tail.head)
          .groupBy(_._1)
          .map(e =>
            e._1 -> e._2
              .map(_._2)
              .toSeq
          )
        queryStringToFilterOptions(query)
      case _                             => Seq.empty
    }

}
