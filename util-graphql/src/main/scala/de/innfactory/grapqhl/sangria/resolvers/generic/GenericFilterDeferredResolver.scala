package de.innfactory.grapqhl.sangria.resolvers.generic

import de.innfactory.grapqhl.sangria.resolvers.models.GenericFilter
import sangria.execution.deferred.{ Deferred, DeferredResolver, UnsupportedDeferError }
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class GenericFilterDeferredResolver[I <: Deferred[Any]: ClassTag, F <: GenericFilter, R, ID, CONTEXT]
    extends DeferredResolver[CONTEXT] {

  private case class TupledFilterInput(filter: F, input: I)
  private case class TupledFilterVectorFilterInput(filter: F, vector: Vector[(F, I)])
  private case class TupledFilterAndIDs(filter: F, ids: Seq[ID])

  implicit private def toTupledFilterInput(v: (F, I)): TupledFilterInput                                      = TupledFilterInput tupled v
  implicit private def toTupledFilterVectorFilterInput(v: (F, Vector[(F, I)])): TupledFilterVectorFilterInput =
    TupledFilterVectorFilterInput tupled v
  implicit private def toTupledFilterAndIDs(v: (F, Seq[ID])): TupledFilterAndIDs                              = TupledFilterAndIDs tupled v

  def toFilter(inputDeferred: I): F

  def deferredToIds(inputDeferred: I): Seq[ID]

  def resultHasID(result: R): ID

  def queryResults(filter: F, idSequence: Seq[ID], context: CONTEXT): Future[Seq[R]]

  override def resolve(deferred: Vector[Deferred[Any]], ctx: CONTEXT, queryState: Any)(implicit
    ec: ExecutionContext
  ): Vector[Future[Seq[R]]] = {

    // Init Resolved and Queries Map
    val resolved =
      scala.collection.mutable
        .Map[Deferred[Any], Future[Seq[R]]]() // After computation contains result for each deferred input
    val queries = scala.collection.mutable.Map[F, Future[Seq[R]]]() // Combined queries by filter

    val singleFilterMap: Map[F, Seq[ID]] = deferred
    // Pattern match to input => resolve has Deferred[Any], but this resolver should only receive deferred of type [I]
    .map { case d: I => d }
    // generate tuple from Filter of Deferred and Deferred
      .map((d: I) => (toFilter(d), d))
      // group by Filter
      .groupBy(_.filter)
      // map to Filter -> Vector[ID] by deferredToIds function
      .map(filterAndVector =>
        filterAndVector.filter -> filterAndVector.vector.map(_.input).flatMap(i => deferredToIds(i))
      )

    singleFilterMap.foreach { mapEntry =>
      val filter: F = mapEntry.filter                         // Get Filter
      val rates     = queryResults(filter, mapEntry.ids, ctx) // query results for Filter and Ids
      queries(filter) = rates // Put result into queries Map
    }

    def filterQueryResultsForDeferredInput(d: I): Future[Seq[R]] = {
      val ids: Seq[ID] = deferredToIds(d)     // Get ids from deferred
      val query        = queries(toFilter(d)) // get query for filter of deferred
      query.map(_.filter(or => ids.contains(resultHasID(or)))) // filter out query result for deferred
    }

    deferred foreach {
      case d: I => resolved(d) = filterQueryResultsForDeferredInput(d)
      case d    => resolved(d) = Future.failed(UnsupportedDeferError(d))
    }

    deferred.map(d => resolved(d)) // Iterate over deferred and replace with resolved for deferred
  }
}
