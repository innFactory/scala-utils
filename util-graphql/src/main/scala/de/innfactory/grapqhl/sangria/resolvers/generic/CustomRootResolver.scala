package de.innfactory.familotel.cms.graphql.resolvers

import de.innfactory.familotel.cms.graphql.models.GraphQLExecutionContext
import de.innfactory.familotel.cms.graphql.resolvers.models.CustomDeferred.Unsupported
import sangria.execution.deferred.{ Deferred, DeferredResolver, UnsupportedDeferError }

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.runtime.universe.typeTag

class CustomRootResolver(deferredConnection: Map[Class[_ <: Deferred[Any]], DeferredResolver[GraphQLExecutionContext]])
    extends DeferredResolver[GraphQLExecutionContext] {

  private val deferredMapper: Map[Class[_ <: Deferred[Any]], DeferredResolver[GraphQLExecutionContext]] =
    deferredConnection

  override def resolve(deferred: Vector[Deferred[Any]], ctx: GraphQLExecutionContext, queryState: Any)(implicit
    ec: ExecutionContext
  ): Vector[Future[Any]] = {

    val grouped: Map[Object, Vector[Deferred[Any]]] =
      deferred groupBy (d => deferredMapper.getOrElse(d.getClass, Unsupported()))

    val resolved: mutable.Map[Deferred[Any], Future[Any]] = scala.collection.mutable.Map[Deferred[Any], Future[Any]]()

    grouped foreach {
      case (_: Unsupported, d)                                      =>
        for (i <- d.indices)
          resolved(d(i)) = Future.failed(UnsupportedDeferError(d(i)))
      case (resolver: DeferredResolver[GraphQLExecutionContext], d) =>
        val res = resolver.resolve(d, ctx, queryState)(ec)
        for (i <- d.indices)
          resolved(d(i)) = res(i)

    }

    deferred map (d => resolved(d))
  }
}
