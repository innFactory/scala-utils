package de.innfactory.familotel.cms.graphql.models.implicits

import play.api.libs.json.{ JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue }
import sangria.ast
import sangria.execution.Executor
import sangria.marshalling.{ ArrayMapBuilder, InputUnmarshaller, ResultMarshaller, ScalarValueInfo }
import sangria.schema._
import sangria.validation.{ BigIntCoercionViolation, IntCoercionViolation, ValueCoercionViolation }
import sangria.macros._
import play.api.libs.json.Json
import sangria.macros.derive.deriveObjectType
object JsonScalarType {

  case object JsonCoercionViolation extends ValueCoercionViolation("Not valid JSON")

  implicit val JsonType = ScalarType[JsValue](
    "Json",
    description = Some("Raw JSON value"),
    coerceOutput = (value, _) ⇒ value,
    coerceUserInput = {
      case v: String     ⇒ Right(JsString(v))
      case v: Boolean    ⇒ Right(JsBoolean(v))
      case v: Int        ⇒ Right(JsNumber(v))
      case v: Long       ⇒ Right(JsNumber(v))
      case v: Float      ⇒ Right(JsNumber(v))
      case v: Double     ⇒ Right(JsNumber(v))
      case v: BigInt     ⇒ Right(JsNumber(BigDecimal.decimal(v.toInt)))
      case v: BigDecimal ⇒ Right(JsNumber(v))
      case v: JsValue    ⇒ Right(v)
    },
    coerceInput = {
      case ast.StringValue(jsonStr, _, _, _, _) ⇒
        Right(Json.parse(jsonStr))
      case _                                    ⇒
        Left(JsonCoercionViolation)
    }
  )

  implicit val JsonMapType = ScalarType[Map[String, JsValue]](
    "JsonMap",
    description = Some("JSON Map value"),
    coerceOutput = (value, _) ⇒ {
      JsObject.apply(value.map(v => (v._1, Json.toJson(v._2))))
    },
    coerceUserInput = {
      case v: JsValue ⇒ Right(Map("" -> v))
    },
    coerceInput = {
      case ast.StringValue(jsonStr, _, _, _, _) ⇒
        Right(Map("" -> Json.parse(jsonStr)))
      case _                                    ⇒
        Left(JsonCoercionViolation)
    }
  )
}
