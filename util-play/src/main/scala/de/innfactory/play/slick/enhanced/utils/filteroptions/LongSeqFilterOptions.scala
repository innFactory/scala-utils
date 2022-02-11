package de.innfactory.play.slick.enhanced.utils.filteroptions

import slick.lifted.Rep

abstract class LongSeqFilterOptions[E] extends FilterOptions[E, Long] {

  override def selector: E => Rep[Long]

  var seq: Option[Seq[Long]] = None

  override def optionsToString: String =
    s"seq=${seq.mkString(", ")} | "

  override def atLeasOneFilterOptionApplicable: Boolean =
    seq.isDefined

  def seqQueryParam: String = queryIdentifier + "Seq"

  def withOptions(
    seqOption: Option[Seq[Long]]
  ): LongSeqFilterOptions[E] = {
    seq = seqOption
    this
  }

  override def getFromQueryString(
    params: Map[String, Seq[String]]
  ): Option[LongSeqFilterOptions[E]] =
    try {
      params.get(seqQueryParam) match {
        case Some(value) => seq = Some(value.map(_.toLong))
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
