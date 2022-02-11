package de.innfactory.play.tracing

import io.opencensus.trace.Span
import play.api.mvc.Request

trait TraceRequest[A] {
  def traceSpan: Span
  def request: Request[A]
}
