package de.innfactory.play.tracing.implicits

import cats.data.EitherT
import de.innfactory.play.controller.ResultStatus
import de.innfactory.play.tracing.TraceContext
import de.innfactory.play.tracing.TracingHelper.traceWithParent

import scala.concurrent.{ ExecutionContext, Future }

object EitherTTracingImplicits {

  implicit class EnhancedTracingEitherT[T](eitherT: EitherT[Future, ResultStatus, T]) {
    def trace[A](
      string: String
    )(implicit rc: TraceContext, ec: ExecutionContext): EitherT[Future, ResultStatus, T] =
      rc.span match {
        case Some(value) =>
          EitherT(traceWithParent(string, value, _ => eitherT.value))
        case None        => eitherT
      }
  }
}
