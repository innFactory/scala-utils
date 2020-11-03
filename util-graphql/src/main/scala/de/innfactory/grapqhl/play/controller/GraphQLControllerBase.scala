package de.innfactory.grapqhl.play.controller

import de.innfactory.grapqhl.play.request.RequestExecutionBase
import de.innfactory.grapqhl.play.request.common.ExecutionHelperBase
import de.innfactory.grapqhl.play.request.implicits.JsValueParser.GraphQLBodyEnhancedJsValue
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import sangria.schema.Schema

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GraphQLControllerBase[S, CTX, RE <: RequestExecutionBase[CTX, S], EH <: ExecutionHelperBase]@Inject()(cc: ControllerComponents)(
                                        services: S,
                                        schema: Schema[CTX, Unit],
                                        authorization: Request[AnyContent] => Either[_, _],
                                        requestExecutor: RE,
                                        executionHelper: EH = new ExecutionHelperBase()
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def graphql: Action[AnyContent] =
    Action.async { request ⇒
      val json: JsValue = request.body.asJson.get // Get the request body as json
      val graphQLBody   = json.getGraphQLBodyParameters(executionHelper)
      requestExecutor.executeQuery(
        graphQLBody.query,
        graphQLBody.variables,
        graphQLBody.operation,
        executionHelper.isTracingEnabled(request),
        request,
        services
      )
    }

  def renderSchema: Action[AnyContent] =
    Action.async { request =>
      val result = for {
        _ <- authorization(request)
      } yield executionHelper.renderSchema(schema)
      Future(result match {
        case Left(_)      => Forbidden("")
        case Right(value) => Ok(value)
      })
    }

}
