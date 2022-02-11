package de.innfactory.play.tracing.implicits

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import de.innfactory.play.tracing.TraceRequest
import io.opencensus.scala.Tracing.traceWithParent
import io.opencensus.trace.Span

import scala.concurrent.{ ExecutionContext, Future }

object EitherTTracingImplicits {

  implicit class EnhancedTracingEitherT[T, V](eitherT: EitherT[Future, V, T]) {
    def trace[A](
                  string: String
                )(implicit traceRequest: TraceRequest[A], ec: ExecutionContext): EitherT[Future, V, T] =
      EitherT(traceWithParent(string, traceRequest.traceSpan) { span =>
        eitherT.value
      })
  }

  def TracedT[A, V](
                  string: String
                )(implicit traceRequest: TraceRequest[A], ec: ExecutionContext): EitherT[Future, V, Span] =
    EitherT(traceWithParent(string, traceRequest.traceSpan) { span =>
      Future(span.asRight[V])
    })

}

