package de.innfactory.grapqhl.play.request.common

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import sangria.renderer.SchemaRenderer
import sangria.schema.Schema

class ExecutionHelperBase {
  def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") Json.obj() else Json.parse(variables).as[JsObject]

  def isTracingEnabled(request: Request[_]) = request.headers.get("X-Tracing").isDefined

  def renderSchema(schema: Schema[_, _]): String = SchemaRenderer.renderSchema(schema)
}
