package de.innfactory.play.tracing

import de.innfactory.play.smithy4play.LogContext
import io.opentelemetry.api.trace.Span
import org.slf4j.{ Marker, MarkerFactory }
import play.api.Logger
import play.api.libs.json.Json

class TraceLogger(span: Option[Span]) {
  private val logger: org.slf4j.Logger = Logger.apply("request-context").logger

  protected def getMarker(span: Span, entity: Option[String])(implicit logContext: LogContext): Marker =
    MarkerFactory.getMarker(spanToMarker(span, entity))

  protected def spanToMarker(span: Span, entity: Option[String])(implicit logContext: LogContext): String =
    Json.prettyPrint(Json.toJson(logContext.toLogbackContext(span.getSpanContext.getTraceId, entity)))

  def warn(message: String, entity: Option[String] = None)(implicit logContext: LogContext): Unit =
    span match {
      case Some(value) => logger.warn(getMarker(value, entity), message)
      case None        => logger.warn(message)
    }

  def error(message: String, entity: Option[String] = None)(implicit logContext: LogContext): Unit =
    span match {
      case Some(value) => logger.error(getMarker(value, entity), message)
      case None        => logger.error(message)
    }

  def info(message: String, entity: Option[String] = None)(implicit logContext: LogContext): Unit =
    span match {
      case Some(value) => logger.info(getMarker(value, entity), message)
      case None        => logger.info(message)
    }

  def debug(message: String, entity: Option[String] = None)(implicit logContext: LogContext): Unit =
    span match {
      case Some(value) => logger.debug(getMarker(value, entity), message)
      case None        => logger.debug(message)
    }
}
