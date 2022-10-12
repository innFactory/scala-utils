package de.innfactory.play.tracing

import com.typesafe.config.Config
import de.innfactory.play.logging.logback.LogbackContext
import io.opentelemetry.api.trace.Tracer

trait ImplicitLogContext {
  implicit val logContext = LogContext(this.getClass.getName)
}

case class LogContext(className: String) {
  def toLogbackContext(traceId: String, entity: Option[String] = None): LogbackContext =
    LogbackContext(className = Some(className), trace = Some(traceId), entity = entity)
  def getTracer(implicit config: Config): Tracer                                       =
    TracingHelper.createTracer(config.getString("project.id"), this.className, "")
}
