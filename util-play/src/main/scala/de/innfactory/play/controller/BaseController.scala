package de.innfactory.play.controller

import akka.stream.scaladsl.Source
import cats.data.EitherT

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{JsError, Json, Reads, Writes}
import play.api.mvc.{AbstractController, Action, ActionBuilder, AnyContent, BodyParser, ControllerComponents, Request, Result, Results => MvcResults}
import cats.instances.future._

import scala.language.implicitConversions

abstract class BaseController(implicit cc: ControllerComponents, ec: ExecutionContext) extends AbstractController(cc) {

  def mapToResult(value: ResultStatus)(implicit ec: ExecutionContext): play.api.mvc.Result =
    value match {
      case e: ErrorResult => MvcResults.Status(e.statusCode)(ErrorResponse.fromMessage(e.message, e.additionalInfoErrorCode))
      case _              => MvcResults.Status(400)("")
    }

  protected def validateJson[A: Reads]: BodyParser[A] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  sealed trait BodyParserI[In]

  case class BodyParserIn[In](bodyParser: BodyParser[In]) extends BodyParserI[In]

  case class BodyParserEmpty[In]() extends BodyParserI[In]

  sealed trait UseCaseL[Domain]

  case class UseCaseLogic[Domain](logic: EitherT[Future, ResultStatus, Domain]) extends UseCaseL[Domain]

  case class UseCaseLogicEmpty[Domain]() extends UseCaseL[Domain]

  implicit def convertToLogic[Domain](f: EitherT[Future, ResultStatus, Domain]): UseCaseLogic[Domain] = UseCaseLogic(f)

  sealed trait OutMapperO[Domain, Out]

  case class OutMapper[Domain, Out](outMapper: Domain => Out) extends OutMapperO[Domain, Out]

  case class OutMapperEmpty[Domain, Out]() extends OutMapperO[Domain, Out]

  sealed trait LogicDomainConverterI[L, D]

  case class LogicDomainConverter[L, D](mapper: L => D) extends LogicDomainConverterI[L, D]

  case class LogicDomainConverterEmpty[L, D]() extends LogicDomainConverterI[L, D]

  object Endpoint {
    def in[V, RequestT[_] <: Request[_], LogicDomain](actionBuilder: ActionBuilder[RequestT, AnyContent])(implicit
      convert: V => LogicDomain,
      reads: Reads[V]
    ): Endpoint[V, Unit, RequestT, Unit, LogicDomain] = {
      val action: ActionBuilder[RequestT, V] = actionBuilder.apply(validateJson[V])
      new Endpoint[V, Unit, RequestT, Unit, LogicDomain](
        action,
        LogicDomainConverter(convert),
        useCaseL = (_, _) => UseCaseLogicEmpty[Unit]()
      )
    }

    def in[RequestT[_] <: Request[_]](
      actionBuilder: ActionBuilder[RequestT, AnyContent]
    ): Endpoint[AnyContent, Unit, RequestT, Unit, Unit] = {
      val action: ActionBuilder[RequestT, AnyContent] = actionBuilder
      new Endpoint[AnyContent, Unit, RequestT, Unit, Unit](
        action,
        LogicDomainConverter(_ => ()),
        useCaseL = (_, _) => UseCaseLogicEmpty[Unit]()
      )

    }
  }

  class Endpoint[
    In,
    Out,
    RequestT[_] <: Request[_],
    Domain,
    DomainAttribute
  ](
    actionBuilder: ActionBuilder[RequestT, In],
    converter: LogicDomainConverterI[In, DomainAttribute] = LogicDomainConverterEmpty(),
    outMapper: OutMapperO[Domain, Out] = OutMapperEmpty[Domain, Out](),
    useCaseL: (DomainAttribute, RequestT[In]) => UseCaseL[Domain] = (a: DomainAttribute, b: RequestT[In]) =>
      UseCaseLogicEmpty[Domain]()
  ) {

    def mapOutTo[OutT](implicit
      outMapperImplicit: OutMapperO[Domain, OutT]
    ): Endpoint[In, OutT, RequestT, Domain, DomainAttribute] =
      new Endpoint[In, OutT, RequestT, Domain, DomainAttribute](
        actionBuilder,
        converter,
        outMapperImplicit,
        useCaseL
      )

    def logic[DomainT](
      useCaseLogic: (DomainAttribute, RequestT[In]) => UseCaseL[DomainT]
    ): Endpoint[In, Out, RequestT, DomainT, DomainAttribute] =
      new Endpoint[In, Out, RequestT, DomainT, DomainAttribute](
        actionBuilder,
        converter,
        OutMapperEmpty[DomainT, Out](),
        useCaseLogic
      )

    def result(completer: EitherT[Future, ResultStatus, Out] => Future[Result]): Action[In] =
      actionBuilder.async { implicit request =>
        completer(useCase(request))
      }

    private def useCase(r: RequestT[In]): EitherT[Future, ResultStatus, Out] =
      converter match {
        case LogicDomainConverter(mapper) =>
          useCaseL(mapper(r.body.asInstanceOf[In]), r) match {
            case UseCaseLogic(logic) =>
              for {
                res <- logic
              } yield outMap(res)
            case _ => throw new IllegalArgumentException("Usecase must include out mapping")
          }
        case _ => throw new IllegalArgumentException("UseCase cannot be Empty")
      }

    private def outMap: Domain => Out =
      outMapper match {
        case OutMapper(outMapper) => outMapper
        case _ => throw new IllegalArgumentException("Out Mapper cannot be Empty")
      }

  }

  implicit class RichResult[T](value: EitherT[Future, ResultStatus, T])(implicit ec: ExecutionContext) {
    def completeResult(statusCode: Int = 200)(implicit writes: Writes[T]): Future[play.api.mvc.Result] =
      value.value.map {
        case Left(error: ResultStatus) => mapToResult(error)
        case Right(value: T)           => MvcResults.Status(statusCode)(Json.toJson(value))
      }

    def completeResultWithoutBody(statusCode: Int = 200): Future[play.api.mvc.Result] =
      value.value.map {
        case Left(error: ResultStatus) => mapToResult(error)
        case Right(_)               => MvcResults.Status(statusCode)("")
      }
  }

  implicit class RichSeqResult[T](value: EitherT[Future, ResultStatus, Seq[T]])(implicit ec: ExecutionContext) {
    def completeResult()(implicit writes: Writes[T]): Future[play.api.mvc.Result] =
      value.value.map {
        case Left(error: ResultStatus) => mapToResult(error)
        case Right(value: Seq[T])      => MvcResults.Status(200)(Json.toJson(value))
      }
  }

  implicit class RichSourceResult[T, V](value: EitherT[Future, ResultStatus, Source[T, V]])(implicit
    ec: ExecutionContext
  ) {
    def completeSourceChunked()(implicit writes: Writes[T]): Future[play.api.mvc.Result] =
      value.value.map {
        case Left(error: ResultStatus)  => mapToResult(error)
        case Right(value: Source[T, _]) =>
          MvcResults
            .Status(200)
            .chunked(
              value.map(Json.toJson(_).toString).intersperse("[", ",", "]"),
              Some("application/json")
            )
        case _                          => MvcResults.Status(500)("could not resolve source")
      }
  }
}
