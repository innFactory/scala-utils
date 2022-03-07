package de.innfactory.play.mail

import play.api.libs.json.Json

case class MailResponse(id: String)

object MailResponse {
  val empty: MailResponse = MailResponse("")

  implicit val format = Json.format[MailResponse]
}
