package de.innfactory.play.results.errors

import de.innfactory.implicits.Showable
import de.innfactory.play.controller.ErrorResult

object Errors {

  case class DatabaseResult(
      message: String = "Entity or request malformed",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 500
  ) extends ErrorResult()
      with Showable

  case class BadRequest(
      message: String = "Entity or request malformed",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 400
  ) extends ErrorResult()
      with Showable

  case class NotFound(
      message: String = "Entity not found",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 404
  ) extends ErrorResult()
      with Showable

  case class Forbidden(
      message: String = "Forbidden",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 403
  ) extends ErrorResult()
      with Showable

  case class TokenValidationError(
      message: String = "TokenValidationError",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 400
  ) extends ErrorResult()
      with Showable

  case class TokenExpiredError(
      message: String = "TokenExpiredError",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 410
  ) extends ErrorResult()
      with Showable

  case class BadGateway(
      message: String = "BadGateway",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 502
  ) extends ErrorResult()
      with Showable

  case class InternalServerError(
      message: String = "InternalServerError",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 500
  ) extends ErrorResult()
      with Showable

  case class TooManyRequests(
      message: String = "TooManyRequests",
      additionalInfoToLog: Option[String] = None,
      additionalInfoErrorCode: Option[String] = None,
      statusCode: Int = 429
  ) extends ErrorResult()
      with Showable

}
