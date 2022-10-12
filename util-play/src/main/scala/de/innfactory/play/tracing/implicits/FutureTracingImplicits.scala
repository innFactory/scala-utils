package de.innfactory.play.tracing.implicits

import com.typesafe.config.Config
import de.innfactory.play.tracing.TraceContext
import de.innfactory.play.tracing.TracingHelper.traceWithParent

import scala.concurrent.{ ExecutionContext, Future }

object FutureTracingImplicits {

  implicit class EnhancedFuture[T](future: Future[T]) {
    def trace(
      string: String
    )(implicit tc: TraceContext, config: Config, ec: ExecutionContext): Future[T] =
      tc.span match {
        case Some(value) =>
          traceWithParent(string, value)(tc.logContext, config, ec) { _ =>
            future
          }
        case None        => future
      }

  }

}
