package de.innfactory.play.mail

import de.innfactory.implicits.Showable
import de.innfactory.play.controller.ErrorResult

case class MailSendError(
                                message: String = "Mailservice had a problem while sending the mail.",
                                additionalInfoToLog: Option[String] = None,
                                additionalInfoErrorCode: Option[String] = None,
                                statusCode: Int = 503
                              ) extends ErrorResult()
  with Showable

object MailSendError {
  def desc = "Mailservice had a problem while sending the mail."
}