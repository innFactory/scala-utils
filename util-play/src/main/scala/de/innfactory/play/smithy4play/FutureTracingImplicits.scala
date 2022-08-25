package de.innfactory.play.smithy4play

import io.opencensus.scala.Tracing.traceWithParent

import scala.concurrent.{ExecutionContext, Future}

object FutureTracingImplicits {

  implicit class EnhancedFuture[T](future: Future[T]) {
    def trace(
               string: String
             )(implicit tc: TraceContext, ec: ExecutionContext): Future[T] =
      tc.span match {
        case Some(value) =>
          traceWithParent(string, value) { _ =>
            future
          }
        case None => future
      }

  }

}
