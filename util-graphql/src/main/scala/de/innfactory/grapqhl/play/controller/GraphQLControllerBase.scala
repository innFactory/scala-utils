package de.innfactory.familotel.cms.controllers

import de.innfactory.familotel.cms.auth.authorization.actions.JwtValidationAndUserExtractionAction
import de.innfactory.familotel.cms.common.implicits.JsValueImplicits.GraphQLBodyEnhancedJsValue
import de.innfactory.familotel.cms.graphql.request.RequestExecution
import de.innfactory.familotel.cms.graphql.request.common.{ ExecutionHelper, ExecutionServices }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class GraphQLController @Inject() (
  cc: ControllerComponents,
  executionServices: ExecutionServices,
  jwtValidationAndUserExtractionAction: JwtValidationAndUserExtractionAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def graphql: Action[AnyContent] =
    Action.async { request ⇒
      val json: JsValue = request.body.asJson.get // Get the request body as json
      val graphQLBody   = json.getGraphQLBodyParameters
      RequestExecution.executeQuery(
        graphQLBody.query,
        graphQLBody.variables,
        graphQLBody.operation,
        ExecutionHelper.isTracingEnabled(request),
        request,
        executionServices
      )
    }

  def renderSchema: Action[AnyContent] =
    Action.async { request =>
      val result = for {
        _ <- jwtValidationAndUserExtractionAction.execute(request)
      } yield ExecutionHelper.renderSchema
      Future(result match {
        case Left(_)      => Forbidden("")
        case Right(value) => Ok(value)
      })
    }

}
