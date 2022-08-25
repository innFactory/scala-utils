package de.innfactory.play.smithy4play

import de.innfactory.play.logging.logback.LogbackContext

trait ImplicitLogContext {
  implicit val logContext = LogContext(this.getClass.getName)
}

case class LogContext(className: String) {
  def toLogbackContext(traceId: String, entity: Option[String] = None): LogbackContext =
    LogbackContext(className = Some(className), trace = Some(traceId), entity = entity)
}
