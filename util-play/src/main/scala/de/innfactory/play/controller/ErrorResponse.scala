package de.innfactory.play.controller

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, Request}

case class ErrorResponse(message: String) {
  def toJson: JsValue = Json.toJson(this)(ErrorResponse.writes)
}

object ErrorResponse {

  implicit val reads  = Json.reads[ErrorResponse]
  implicit val writes = Json.writes[ErrorResponse]

  def fromRequest(message: String)(implicit request: Request[AnyContent]) =
    Json.toJson(ErrorResponse(message))

  def fromMessage(message: String) =
    Json.toJson(ErrorResponse(message))

}

