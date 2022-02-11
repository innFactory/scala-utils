package de.innfactory.play.controller

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Request}

case class ErrorResponse(message: String, errorCode: Option[String] = None) {
  def toJson: JsValue = Json.toJson(this)(ErrorResponse.writes)
}

object ErrorResponse {

  implicit val reads  = Json.reads[ErrorResponse]
  implicit val writes = Json.writes[ErrorResponse]

  def fromRequest(message: String, errorCode: Option[String] = None)(implicit request: Request[AnyContent]) =
    Json.toJson(ErrorResponse(message, errorCode))

  def fromMessage(message: String, errorCode: Option[String] = None) =
    Json.toJson(ErrorResponse(message, errorCode))

}

