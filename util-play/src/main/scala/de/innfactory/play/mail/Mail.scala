package de.innfactory.play.mail

import play.api.libs.json.{JsObject, Json}


case class Mail(
                        recipients: Seq[String],
                        toCC: Option[Seq[String]],
                        toBCC: Option[Seq[String]],
                        replyTo: Option[String],
                        body: String,
                        fromName: String,
                        subject: String,
                        fromEmail: String
                      ) {

  def toRequestJson: JsObject = ???

}

object Mail {
  implicit val format = Json.format[Mail]
}
