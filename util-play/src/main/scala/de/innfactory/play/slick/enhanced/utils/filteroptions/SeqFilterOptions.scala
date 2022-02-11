package de.innfactory.play.slick.enhanced.utils.filteroptions

import slick.lifted.Rep

abstract class SeqFilterOptions[E] extends FilterOptions[E, String] {

  override def selector: E => Rep[String]

  var seq: Option[Seq[String]] = None

  override def optionsToString: String =
    s"seq=${seq.mkString(", ")} | "

  override def atLeasOneFilterOptionApplicable: Boolean =
    seq.isDefined

  def seqQueryParam: String = queryIdentifier + "Seq"

  def withOptions(
    seqOption: Option[Seq[String]]
  ): SeqFilterOptions[E] = {
    seq = seqOption
    this
  }

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[SeqFilterOptions[E]] =
    try {
      params.get(seqQueryParam) match {
        case Some(value) => seq = Some(value)
        case None        => seq = None
      }
      Some(
        withOptions(
          seq
        )
      )
    } catch {
      case _: Exception => None
    }

}
