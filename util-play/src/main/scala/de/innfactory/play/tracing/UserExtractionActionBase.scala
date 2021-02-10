package de.innfactory.play.tracing

import play.api.Environment
import play.api.mvc.{ActionRefiner, BodyParsers, Result}

import scala.concurrent.{ExecutionContext, Future}

abstract class UserExtractionActionBase[R[B] <: TraceRequest[B], +P[B] <: TraceRequest[B]](implicit
                                                                                           val environment: Environment,
                                                                                           val parser: BodyParsers.Default,
                                                                                           val executionContext: ExecutionContext
                                                                                          ) extends ActionRefiner[R, P] {

  override protected def refine[A](request: R[A]): Future[Either[Result, P[A]]] =
    Future.successful {
      extractUserAndCreateNewRequest(request)
    }.flatten

  def extractUserAndCreateNewRequest[A](request: R[A])(implicit
                                                       environment: Environment,
                                                       parser: BodyParsers.Default,
                                                       executionContext: ExecutionContext
  ): Future[Either[Result, P[A]]]

}
