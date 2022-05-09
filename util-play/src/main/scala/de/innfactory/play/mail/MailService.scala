package de.innfactory.play.mail

import cats.data.EitherT

import scala.concurrent.Future
import com.google.inject.ImplementedBy
import de.innfactory.implicits.OptionUtils.EnhancedOption
import de.innfactory.play.controller.ResultStatus
import de.innfactory.play.results.Results.Result
import play.api.Logger

trait MailService {

  val logger = Logger("mail").logger

  def sendAsOption(mail: Mail): Future[Option[MailResponse]]

  def send(mail: Mail): Future[Result[MailResponse]]

  def sendAsEitherT(mail: Mail): EitherT[Future, MailSendError, MailResponse]

}
