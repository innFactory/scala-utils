package de.innfactory.play.tracing

import io.opencensus.trace.Span
import play.api.mvc.{Request, WrappedRequest}

class RequestWithTrace[A](val traceSpan: Span, val request: Request[A])
  extends WrappedRequest[A](request)
    with TraceRequest[A]
