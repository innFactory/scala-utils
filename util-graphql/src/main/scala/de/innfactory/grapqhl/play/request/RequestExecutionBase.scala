package de.innfactory.familotel.cms.graphql.request

import cats.data.EitherT
import cats.implicits._
import de.innfactory.familotel.cms.graphql.exception.ExceptionHandling.{ exceptionHandler, TooComplexQueryError }
import de.innfactory.familotel.cms.graphql.models.GraphQLExecutionContext
import de.innfactory.familotel.cms.graphql.request.common.ExecutionServices
import de.innfactory.familotel.cms.graphql.schema.SchemaDefinition
import play.api.libs.json._
import play.api.mvc.Results.{ BadRequest, InternalServerError, Ok }
import play.api.mvc._
import sangria.execution._
import sangria.parser.{ QueryParser, SyntaxError }
import sangria.marshalling.playJson._
import sangria.slowlog.SlowLog

import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }

object RequestExecution {

  def executeQuery(
    query: String,
    variables: Option[JsObject],
    operation: Option[String],
    tracing: Boolean,
    request: Request[AnyContent],
    executionServices: ExecutionServices
  )(implicit ec: ExecutionContext): Future[Result] = {

    val context =
      GraphQLExecutionContext(
        request,
        executionServices.viomaBookingsRepository,
        executionServices.hotelRepository,
        executionServices.groupRepository,
        executionServices.occupancyRatesRepository,
        executionServices.forecastRateRepository,
        executionServices.feedRunsRepository,
        executionServices.usersRepository
      )

    QueryParser.parse(query) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst)           ⇒
        Executor
          .execute(
            SchemaDefinition.BookingsSchema,
            queryAst,
            context,
            operationName = operation,
            variables = variables getOrElse Json.obj(),
            exceptionHandler = exceptionHandler,
            deferredResolver = SchemaDefinition.resolvers,
            queryReducers = List(
              QueryReducer.rejectMaxDepth[GraphQLExecutionContext](15),
              QueryReducer.rejectComplexQueries[GraphQLExecutionContext](4000, (_, _) ⇒ TooComplexQueryError)
            ),
            middleware = if (tracing) SlowLog.extension :: Nil else Nil
          )
          .map(Ok(_))
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
            case error: ErrorWithResolver  ⇒ InternalServerError(error.resolveError)
          }
      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) ⇒
        Future.successful(
          BadRequest(
            Json.obj(
              "syntaxError" → error.getMessage,
              "locations"   → Json.arr(
                Json.obj("line" → error.originalError.position.line, "column" → error.originalError.position.column)
              )
            )
          )
        )

      case Failure(error)              ⇒
        throw error
    }
  }
}
