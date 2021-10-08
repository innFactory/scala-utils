package de.innfactory.play.slick.enhanced.query

import de.innfactory.play.db.codegen.XPostgresProfile
import de.innfactory.play.slick.enhanced.utils.filteroptions.{BooleanFilterOptions, DoubleFilterOptions, FilterOptions, IntFilterOptions, LongFilterOptions, LongSeqFilterOptions, OptionStringFilterOptions, SeqFilterOptions, StringFilterOptions}
import play.api.Logger
import slick.lifted.StringColumnExtensionMethods

import scala.reflect.runtime.universe._

object EnhancedQuery {

  val profile = XPostgresProfile
  import profile.api._

  implicit class EnhancedQuery[I, F](
    query: Query[I, F, Seq]
  ) {

    def filterOptions[T <: FilterOptions[I, _]](
      seq: Seq[FilterOptions[I, _]]
    ): Query[I, F, Seq] = {
      seq.foreach { x =>
        Logger("EnhancedQuery").logger
          .debug(s"FilterOptions | ${x.queryIdentifier} | ${x.atLeasOneFilterOptionApplicable} | ${x.optionsToString}")
      }
      applyFilterOptions(query, seq)
    }

    private def applyFilterOptions[T <: FilterOptions[I, _]](
      processedQuery: Query[I, F, Seq],
      seq: Seq[FilterOptions[I, _]]
    ): Query[I, F, Seq] =
      seq.length match {
        case 0 => processedQuery
        case _ => process(processedQuery, seq.head, seq)
      }

    private def process[V: TypeTag, T <: FilterOptions[I, V]](
      processedQuery: Query[I, F, Seq],
      element: FilterOptions[I, V],
      seq: Seq[FilterOptions[I, _]]
    ): Query[I, F, Seq] =
      element match {
        case option: IntFilterOptions[I]          => applyFilterOptions(processedQuery.filterOptIntOptions(option), seq.tail)
        case option: StringFilterOptions[I]       =>
          applyFilterOptions(processedQuery.filterOptStringOptions(option), seq.tail)
        case option: OptionStringFilterOptions[I] =>
          applyFilterOptions(processedQuery.filterOptOptionStringOptions(option), seq.tail)
        case option: LongFilterOptions[I]         => applyFilterOptions(processedQuery.filterOptLongOptions(option), seq.tail)
        case option: DoubleFilterOptions[I]       =>
          applyFilterOptions(processedQuery.filterOptDoubleOptions(option), seq.tail)
        case option: SeqFilterOptions[I]          => applyFilterOptions(processedQuery.filterOptSeqString(option), seq.tail)
        case option: LongSeqFilterOptions[I]      => applyFilterOptions(processedQuery.filterOptSeqLong(option), seq.tail)
        case option: BooleanFilterOptions[I]      => applyFilterOptions(processedQuery.filterOptBooleanOptions(option), seq.tail)
        case o                                    => {
          Logger.apply("EnhancedQuery").logger.error("process filterOptions | unknown Option: " + o)
          applyFilterOptions(query, seq.tail)
        }
      }

    def filterOptStringOptions[T <: StringFilterOptions[I]](
      stringFilterOptions: StringFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[String] = stringFilterOptions.selector
      query
        .filterOptStringStartsWith(stringFilterOptions.startsWithOption.value)
        .filterOptStringEndsWith(stringFilterOptions.endsWithOption.value)
        .filterOptStringIncludes(stringFilterOptions.includesOption.value)
        .filterOptStringNotIncludes(stringFilterOptions.notIncludesOption.value)
        .filterOptStringEquals(stringFilterOptions.equalsOption.value)
        .filterOptStringNotEquals(stringFilterOptions.notEqualsOption.value)
    }

    def filterOptOptionStringOptions[T <: OptionStringFilterOptions[I]](
      stringFilterOptions: OptionStringFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Option[String]] = stringFilterOptions.selector
      query
        .filterOptStringStartsWithForRepOption(stringFilterOptions.startsWithOption.value)
        .filterOptStringEndsWithForRepOption(stringFilterOptions.endsWithOption.value)
        .filterOptStringIncludesForRepOption(stringFilterOptions.includesOption.value)
        .filterOptStringNotIncludesForRepOption(stringFilterOptions.notIncludesOption.value)
        .filterOptStringEqualsForRepOption(stringFilterOptions.equalsOption.value)
        .filterOptStringNotEqualsForRepOption(stringFilterOptions.notEqualsOption.value)
    }

    /* SEQ STRING OPTIONS */

    def filterOptSeqString[T <: SeqFilterOptions[I]](
      seqFilterOptions: SeqFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[String] = seqFilterOptions.selector
      query
        .filterOptInSeq(seqFilterOptions.seq)
    }

    /* Boolean OPTIONS */

    def filterOptBooleanOptions[T <: BooleanFilterOptions[I]](
                                                      booleanFilterOption: BooleanFilterOptions[I]
                                                    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Boolean] = booleanFilterOption.selector
      query.filterOptBoolean(booleanFilterOption.equalsOption.value)
    }

    /* SEQ LONG OPTIONS */

    def filterOptSeqLong[T <: LongSeqFilterOptions[I]](
      seqFilterOptions: LongSeqFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Long] = seqFilterOptions.selector
      query
        .filterOptLongInSeq(seqFilterOptions.seq)
    }

    /* INT OPTIONS */

    def filterOptIntOptions[T <: IntFilterOptions[I]](
      intFilterOptions: IntFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Int] = intFilterOptions.selector
      query
        .filterOptIntMin(intFilterOptions.minOption.value)
        .filterOptIntMax(intFilterOptions.maxOption.value)
    }

    def filterOptIntMin(
      option: Option[Int]
    )(implicit
      selector: I => Rep[Int]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) >= v)

    def filterOptIntMax(
      option: Option[Int]
    )(implicit
      selector: I => Rep[Int]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) <= v)

    /* DOUBLE OPTIONS */

    def filterOptDoubleOptions[T <: DoubleFilterOptions[I]](
      doubleFilterOptions: DoubleFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Double] = doubleFilterOptions.selector
      query
        .filterOptDoubleMin(doubleFilterOptions.minOption.value)
        .filterOptDoubleMax(doubleFilterOptions.maxOption.value)
    }

    def filterOptDoubleMin(
      option: Option[Double]
    )(implicit
      selector: I => Rep[Double]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) >= v)

    def filterOptDoubleMax(
      option: Option[Double]
    )(implicit
      selector: I => Rep[Double]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) <= v)

    /* LONG OPTIONS */

    def filterOptLongInSeq(
      option: Option[Seq[Long]]
    )(implicit selector: I => Rep[Long]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r).inSet(v))

    def filterOptLongOptions[T <: LongFilterOptions[I]](
      longFilterOptions: LongFilterOptions[I]
    ): Query[I, F, Seq] = {
      implicit val selector: I => Rep[Long] = longFilterOptions.selector
      query
        .filterOptLongMin(longFilterOptions.minOption.value)
        .filterOptLongMax(longFilterOptions.maxOption.value)
    }

    def filterOptLongMin(
      option: Option[Long]
    )(implicit
      selector: I => Rep[Long]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) >= v)

    def filterOptLongMax(
      option: Option[Long]
    )(implicit
      selector: I => Rep[Long]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) <= v)

    /* REP[BOOLEAN] OPTIONS */

    def filterOptBoolean(
                        option: Option[Boolean]
                      )(implicit selector: I => Rep[Boolean]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) === v)

    /* REP[STRING] OPTIONS */

    def filterOptInSeq(
      option: Option[Seq[String]]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r).inSet(v))

    def filterOptStringStartsWith(
      option: Option[String]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r).startsWith(v))

    def filterOptStringEndsWith(
      option: Option[String]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r).endsWith(v))

    def filterOptStringIncludes(
      option: Option[String]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r).like("%" + v + "%"))

    def filterOptStringNotIncludes(
      option: Option[String]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => !selector(r).like("%" + v + "%"))

    def filterOptStringEquals(
      option: Option[String]
    )(implicit selector: I => Rep[String]): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) === v)

    def filterOptStringNotEquals(
      option: Option[String]
    )(implicit
      selector: I => Rep[String]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) => selector(r) =!= v)

    /* REP[OPTION[STRING]] OPTIONS */

    def filterOptStringStartsWithForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c.startsWith(v)
      )

    def filterOptStringEndsWithForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c.endsWith(v)
      )

    def filterOptStringIncludesForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c.like("%" + v + "%")
      )

    def filterOptStringNotIncludesForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        !new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c.like("%" + v + "%")
      )

    def filterOptStringEqualsForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c === v
      )

    def filterOptStringNotEqualsForRepOption(
      option: Option[String]
    )(implicit
      selector: I => Rep[Option[String]]
    ): Query[I, F, Seq] =
      query.filterOpt(option)((r, v) =>
        new StringColumnExtensionMethods[String](selector(r).asInstanceOf[Rep[String]]).c =!= v
      )

  }

}
