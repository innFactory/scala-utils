package de.innfactory.play.smithy4play

import play.api.libs.json.{JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue}
import smithy4s.Document

object PlayJsonToDocumentMapper {

  def mapToDocument(jsValue: JsValue): Document =
    jsValue match {
      case JsNull               => Document.nullDoc
      case boolean: JsBoolean   => Document.fromBoolean(boolean.value)
      case JsNumber(value)      => Document.fromBigDecimal(value)
      case JsString(value)      => Document.fromString(value)
      case JsArray(value)       => Document.array(value.map(mapToDocument))
      case JsObject(underlying) => Document.obj(underlying.map(t => (t._1, mapToDocument(t._2))).toSeq: _*)
    }

  def documentToJsValue(document: Document): JsValue =
    document match {
      case Document.DNumber(value)  => JsNumber(value)
      case Document.DString(value)  => JsString(value)
      case Document.DBoolean(value) => JsBoolean(value)
      case Document.DNull           => JsNull
      case Document.DArray(value)   => JsArray(value.map(documentToJsValue))
      case Document.DObject(value)  => JsObject.apply(value.map(t => (t._1, documentToJsValue(t._2))))
    }

}
