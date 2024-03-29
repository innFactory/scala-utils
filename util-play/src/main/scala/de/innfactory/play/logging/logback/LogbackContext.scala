package de.innfactory.play.logging.logback

import play.api.libs.json.Json

case class LogbackContext (trace: Option[String], className: Option[String], entity: Option[String])

object LogbackContext {
  implicit val writer = Json.writes[LogbackContext]
  implicit val reads = Json.reads[LogbackContext]


  def parse(s: String): LogbackContext = {
    if(Json.parse(s).validate[Seq[LogbackContext]].isSuccess){
      Json.parse(s).as[Seq[LogbackContext]].head
    } else {
      Json.parse(s).as[LogbackContext]
    }
  }

}
