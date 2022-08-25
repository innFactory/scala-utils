package de.innfactory.play.smithy4play

import io.opencensus.trace.Span

trait TraceContext extends ImplicitLogContext with ContextWithHeaders {
  def httpHeaders: HttpHeaders
  def span: Option[Span]

  private val traceLogger = new TraceLogger(span)
  final def log: TraceLogger = traceLogger

  def logIfDebug(message: String, entity: Option[String] = None): Unit =
    if (isDebug) traceLogger.debug(message, entity)

  def isDebug: Boolean = httpHeaders.getHeader("x-app-debug").contains("true")
}