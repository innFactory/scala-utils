package de.innfactory.familotel.cms.graphql.exception

import sangria.execution.{ ExceptionHandler, HandledException, MaxQueryDepthReachedError, UserFacingError }
import sangria.marshalling.ResultMarshaller
import sangria.validation.Violation

object ExceptionHandling {

  case object TooComplexQueryError extends Exception("Query is too expensive.")

  lazy val exceptionHandler: ExceptionHandler = ExceptionHandler(
    onException = onException,
    onUserFacingError = onUserFacingError,
    onViolation = onViolation
  )

  private lazy val onException: PartialFunction[(ResultMarshaller, Throwable), HandledException] = {
    case (_, error @ TooComplexQueryError)         ⇒ HandledException(error.getMessage)
    case (_, error @ MaxQueryDepthReachedError(_)) ⇒ HandledException(error.getMessage)
    case (m, error)                               =>
      HandledException(
        error.getMessage,
        Map(
          "ERROR_CODE" → m.scalarNode(500, "Int", Set.empty)
        )
      )
  }

  private lazy val onUserFacingError: PartialFunction[(ResultMarshaller, UserFacingError), HandledException] = {
    case (m, error) =>
      HandledException(
        error.getMessage,
        Map(
          "ERROR_CODE"    → m.scalarNode(
            error.getMessage match {
              case "Forbidden"           => 403
              case "TooManyRequests"     => 429
              case "InternalServerError" => 500
              case "BadRequest"          => 400
              case "NotFound"            => 404
              case "Unauthorized"        => 401
              case _                     => 400
            },
            "Int",
            Set.empty
          ),
          "ERROR_MESSAGE" → m.scalarNode(error.getMessage, "String", Set.empty)
        )
      )
  }

  private lazy val onViolation: PartialFunction[(ResultMarshaller, Violation), HandledException] = {
    case (m, error) =>
      HandledException(
        error.errorMessage,
        Map(
          "ERROR_CODE" → m.scalarNode(400, "Int", Set.empty)
        )
      )
  }

}
