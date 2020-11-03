package de.innfactory.grapqhl.sangria.implicits

import play.api.libs.json._
import sangria.ast
import sangria.schema._
import sangria.validation.ValueCoercionViolation

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
