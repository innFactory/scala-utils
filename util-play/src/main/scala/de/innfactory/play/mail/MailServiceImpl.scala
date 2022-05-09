package de.innfactory.play.mail
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import de.innfactory.play.results.Results.Result
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MailServiceImpl @Inject()(mailConfig: MailConfig, wsClient: WSClient)(implicit ec: ExecutionContext) extends MailService {

  override def sendAsOption(mail: Mail): Future[Option[MailResponse]] = send(mail).map(_.toOption)

  override def send(mail: Mail): Future[Result[MailResponse]] = sendAsEitherT(mail).value

  override def sendAsEitherT(mail: Mail): EitherT[Future, MailSendError, MailResponse] = {
    val request: WSRequest = wsClient.url(mailConfig.endpoint + "/v1/sendmail")
      .addHttpHeaders(("Authorization", mailConfig.apiKey))

    EitherT(
       request.post(mail.toRequestJson)
        .map(response => {
        if (response.status == 200) {
          MailResponse(response.body.replace("\"", "")).asRight
        } else {
          Left(MailSendError(MailSendError.desc + s" (${response.status})"))
        }
      }))
  }
}
