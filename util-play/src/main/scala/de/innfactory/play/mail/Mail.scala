package de.innfactory.play.mail

import play.api.libs.json.{JsObject, Json}


case class Mail(
                 to: Seq[String],
                 cc: Option[Seq[String]] = None,
                 bcc: Option[Seq[String]] = None,
                 replyTo: Option[String] = None,
                 body: String,
                 fromName: String = "innFactory Mail Gateway",
                 subject: String,
                 from: String = "mail@innfactory.cloud"
                      ) {

  def toRequestJson: JsObject = {
    Json.obj(
      "recipients" -> to,
      "toCC" -> cc,
      "toBCC" -> bcc,
      "replyTo" -> replyTo,
      "body" -> body,
      "fromName" -> fromName,
      "subject" -> subject,
      "fromEmail" -> from
    )
  }

}

object Mail {
  implicit val format = Json.format[Mail]
}
