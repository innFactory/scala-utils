package de.innfactory.play.smithy4play

import cats.data.EitherT
import de.innfactory.play.controller.ResultStatus
import io.opencensus.scala.Tracing.traceWithParent

import scala.concurrent.{ExecutionContext, Future}


object EitherTTracingImplicits {

  implicit class EnhancedTracingEitherT[T](eitherT: EitherT[Future, ResultStatus, T]) {
    def trace[A](
                  string: String
                )(implicit rc: TraceContext, ec: ExecutionContext): EitherT[Future, ResultStatus, T] =
      rc.span match {
        case Some(value) =>
          EitherT(traceWithParent(string, value) { span =>
            eitherT.value
          })
        case None => eitherT
      }

  }

}

