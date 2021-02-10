package de.innfactory.play.tracing


import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc._
import cats.implicits._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseAuthHeaderRefineAction[R[B] <: TraceRequest[B]] @Inject() (parser: BodyParsers.Default)(implicit
                                                                                                               ec: ExecutionContext
) extends ActionFilter[R] {

  val header = "Authorization"

  override protected def executionContext: ExecutionContext = ec

  override def filter[A](request: R[A]): Future[Option[Result]] =
    Future({
      if (extractAndCheckAuthHeader(request.request.headers).getOrElse(false))
        None
      else if (request.request.headers.get("Authorization").isEmpty)
        Some(Unauthorized("Unauthorized"))
      else
        Some(Forbidden("Forbidden"))
    })

  /**
   * Extract auth header from requestHeaders
   *
   * @param requestHeader
   * @return
   */
  def extractAndCheckAuthHeader(requestHeader: Headers) =
    for {
      header <- requestHeader.get(header)
    } yield checkAuthHeader(header)

  /**
   * check and validate auth header
   *
   * @param authHeader
   * @return
   */
  def checkAuthHeader(authHeader: String): Boolean

}

