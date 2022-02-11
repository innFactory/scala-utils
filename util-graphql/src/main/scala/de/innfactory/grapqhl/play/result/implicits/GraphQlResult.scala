package de.innfactory.grapqhl.play.result.implicits


import sangria.execution.UserFacingError

import scala.concurrent.{ ExecutionContext, Future }

trait GraphQlException extends Exception with UserFacingError

trait ErrorParser[E] {
  def internalErrorToUserFacingError(error: E): GraphQlException
}

object GraphQlResult {
  implicit class EnhancedFutureResult[E, T](value: Future[Either[E, T]]) {
    def completeOrThrow(implicit ec: ExecutionContext, errorParser: ErrorParser[E]): Future[T] =
      value.map(_.completeOrThrow)
  }

  implicit class EnhancedResult[E, T](value: Either[E, T]) {
    def completeOrThrow(implicit errorParser: ErrorParser[E]): T =
      if (value.isRight)
        value.right.get
      else
        throw errorParser.internalErrorToUserFacingError(value.left.get)
  }

  case class InternalServerError(msg: String) extends GraphQlException {
    override def getMessage: String = msg
  }
  case class ForbiddenError(msg: String)      extends GraphQlException {
    override def getMessage: String = msg
  }
  case class BadRequestError(msg: String)     extends GraphQlException {
    override def getMessage: String = msg
  }
  case class NotFoundError(msg: String)       extends GraphQlException {
    override def getMessage: String = msg
  }
  case class UnauthorizedError(msg: String)   extends GraphQlException {
    override def getMessage: String = msg
  }

}
