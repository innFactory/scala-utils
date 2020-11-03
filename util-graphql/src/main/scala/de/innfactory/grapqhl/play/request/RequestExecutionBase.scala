package de.innfactory.grapqhl.play.request

import de.innfactory.grapqhl.play.exception.ExceptionHandling
import de.innfactory.grapqhl.play.exception.ExceptionHandling.TooComplexQueryError
import play.api.libs.json._
import play.api.mvc.Results.{BadRequest, InternalServerError, Ok}
import play.api.mvc._
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema
import sangria.slowlog.SlowLog
import de.innfactory.grapqhl.sangria.marshalling.playJson._

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}

abstract class RequestExecutionBase[CTX <: Any, S](schema: Schema[Any, Any], exceptionHandler: ExceptionHandler = ExceptionHandling.exceptionHandler, resolvers: DeferredResolver[Any] = DeferredResolver.empty, deprecationTracker: DeprecationTracker =DeprecationTracker.empty, middleware: List[Middleware[Any]] = Nil, additionalQueryReducers: List[QueryReducer[Any, CTX]] = Nil) {

  // Query Reducers Max Depth Of Request
  val queryReducerMaxDepth: Int = 15
  // Query Reducers Max Complexity For Request
  val queryReducerComplexityThreshold: Int = 4000

  // Predefined Query Reducers
  val baseQueryReducers: List[QueryReducer[Any, _]] = List(
    QueryReducer.rejectMaxDepth[Any](queryReducerMaxDepth),
    QueryReducer.rejectComplexQueries[Any](queryReducerComplexityThreshold, (_, _) ⇒ TooComplexQueryError)
  )

  def handleSyntaxError(error: SyntaxError): Future[Result] = {
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
  }

  def contextBuilder(services: S, request: Request[AnyContent]): CTX

  def executeQuery(
    query: String,
    variables: Option[JsObject],
    operation: Option[String],
    tracing: Boolean,
    request: Request[AnyContent],
    services: S
  )(implicit ec: ExecutionContext): Future[Result] = {
    val context: CTX = contextBuilder(services, request)
    QueryParser.parse(query) match {
      case Success(queryAst)           ⇒
        Executor
          .execute(
            schema,
            queryAst,
            context,
            operationName = operation,
            variables = variables getOrElse Json.obj(),
            exceptionHandler = exceptionHandler,
            deprecationTracker = deprecationTracker,
            deferredResolver = resolvers,
            queryReducers = baseQueryReducers ++ additionalQueryReducers,
            middleware = if (tracing) SlowLog.extension :: middleware else middleware
          )
          .map(Ok(_))
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
            case error: ErrorWithResolver  ⇒ InternalServerError(error.resolveError)
          }
      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) ⇒ handleSyntaxError(error)
      case Failure(error)              ⇒ throw error
    }
  }
}
