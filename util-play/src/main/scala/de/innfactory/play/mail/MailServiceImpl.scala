package de.innfactory.play.mail
import de.innfactory.implicits.OptionUtils.EnhancedOption
import de.innfactory.play.controller.ResultStatus
import de.innfactory.play.results.Results
import de.innfactory.play.results.Results.Result
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MailServiceImpl @Inject()(mailConfig: MailConfig, wsClient: WSClient)(implicit ec: ExecutionContext) extends MailService {


  override def sendF(mail: Mail): Future[Option[MailResponse]] = {
    val request: WSRequest = wsClient.url(mailConfig.endpoint + "/v1/sendmail")
    request.post(mail.toRequestJson)
      .map(response => {
        if (response.status == 200) {
          response.json.asOpt[MailResponse]
        } else {
          None
        }
      })
  }

  override def sendE(mail: Mail): Future[Result[MailResponse]] = sendF(mail).map(_.toResult(MailSendError())) // TODO: implement error handling
}
