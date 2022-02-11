package de.innfactory.play.mail

import cats.data.EitherT

import scala.concurrent.Future
import com.google.inject.ImplementedBy
import de.innfactory.implicits.OptionUtils.EnhancedOption
import de.innfactory.play.controller.ResultStatus
import de.innfactory.play.results.Results.Result
import play.api.Logger

@ImplementedBy(classOf[MailServiceImpl])
trait MailService {

  val logger = Logger("mail").logger

  def sendF(mail: Mail): Future[Option[MailResponse]]

  def sendE(mail: Mail): Future[Result[MailResponse]]

  def sendET(mail: Mail): EitherT[Future, Result, MailResponse] = EitherT(sendE(mail))

}