package de.innfactory.grapqhl.play.request.implicits

import de.innfactory.grapqhl.play.request.common.ExecutionHelperBase
import play.api.libs.json.{JsObject, JsString, JsValue}

  object JsValueParser {

    case class GraphQlBody(query: String, operation: Option[String], variables: Option[JsObject])

    implicit def graphQlBodyTupled(bodyTuple: (String, Option[String], Option[JsObject])): GraphQlBody =
      GraphQlBody tupled bodyTuple

    implicit class GraphQLBodyEnhancedJsValue(jsValue: JsValue) {
      def getQuery: String                 = (jsValue \ "query").as[String]
      def getOperationName: Option[String] = (jsValue \ "operationName").asOpt[String]
      def getVariables[EH <: ExecutionHelperBase](implicit executionHelper: EH): Option[JsObject]   =
        (jsValue \ "variables").toOption.flatMap {
          case JsString(vars) ⇒ Some(executionHelper.parseVariables(vars))
          case obj: JsObject  ⇒ Some(obj)
          case _              ⇒ None
        }

      def getGraphQLBodyParameters[EH <: ExecutionHelperBase](implicit executionHelper: EH): GraphQlBody = (getQuery, getOperationName, getVariables)
    }
  }

