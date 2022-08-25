package de.innfactory.play.common

import cats.data.{ EitherT, Validated }
import de.innfactory.play.controller.ResultStatus

import scala.concurrent.{ ExecutionContext, Future }

object ValidT {

  def apply(boolean: Boolean, notValid: ResultStatus)(implicit
    ec: ExecutionContext
  ): EitherT[Future, ResultStatus, Unit] =
    EitherT(
      Future(
        Validated.cond(boolean, (), notValid).toEither
      )
    )

}
