package de.innfactory.grapqhl.sangria.resolvers.generic

import de.innfactory.grapqhl.sangria.resolvers.models.CustomDeferred.Unsupported
import sangria.execution.deferred.{Deferred, DeferredResolver, UnsupportedDeferError}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class CustomRootResolver[CTX](deferredConnection: Map[Class[_ <: Deferred[Any]], DeferredResolver[CTX]])
    extends DeferredResolver[CTX] {

  private val deferredMapper: Map[Class[_ <: Deferred[Any]], DeferredResolver[CTX]] =
    deferredConnection

  override def resolve(deferred: Vector[Deferred[Any]], ctx: CTX, queryState: Any)(implicit
    ec: ExecutionContext
  ): Vector[Future[Any]] = {

    val resolved: mutable.Map[Deferred[Any], Future[Any]] = scala.collection.mutable.Map[Deferred[Any], Future[Any]]()

    def handleUnsupported(d: Vector[Deferred[Any]], resolvedMap: mutable.Map[Deferred[Any], Future[Any]]): Unit = {
      for (i <- d.indices)
        resolvedMap(d(i)) = Future.failed(UnsupportedDeferError(d(i)))
    }

    def handleSupportedDeferred(d: Vector[Deferred[Any]], resolver: DeferredResolver[CTX], resolvedMap: mutable.Map[Deferred[Any], Future[Any]]): Unit = {
      val res = resolver.resolve(d, ctx, queryState)(ec)
      for (i <- d.indices)
        resolvedMap(d(i)) = res(i)
    }

    // group deferred by resolver if available else by 'Unsupported()'
    val grouped: Map[Object, Vector[Deferred[Any]]] =
      deferred groupBy (d => deferredMapper.getOrElse(d.getClass, Unsupported()))

    // Handle each Deferred Group
    grouped foreach {
      case (_: Unsupported, d)                                      => handleUnsupported(d, resolved)
      case (resolver: DeferredResolver[CTX], d) =>                     handleSupportedDeferred(d, resolver, resolved)
    }

    // replace deferred in vector by result in resolved map
    deferred map (d => resolved(d))
  }
}
