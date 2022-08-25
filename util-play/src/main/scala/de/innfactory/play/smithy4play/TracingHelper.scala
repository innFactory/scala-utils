package de.innfactory.play.smithy4play

import de.innfactory.play.tracing.GoogleTracingIdentifier.{
  XTRACINGID,
  X_INTERNAL_SPANID,
  X_INTERNAL_TRACEID,
  X_INTERNAL_TRACEOPTIONS
}
import io.opencensus.scala.Tracing.startSpanWithRemoteParent
import io.opencensus.trace.{ Span, SpanContext, SpanId, TraceId, TraceOptions, Tracestate }

object TracingHelper {

  def generateSpanFromRemoteSpan[A](headers: HttpHeaders): Option[Span] = {
    val headerTracingIdOptional = headers.getHeader(XTRACINGID)
    val spanIdOptional          = headers.getHeader(X_INTERNAL_SPANID)
    val traceIdOptional         = headers.getHeader(X_INTERNAL_TRACEID)
    val traceOptionsOptional    = headers.getHeader(X_INTERNAL_TRACEOPTIONS)
    val span                    = for {
      headerTracingId <- headerTracingIdOptional
      spanId          <- spanIdOptional
      traceId         <- traceIdOptional
      traceOptions    <- traceOptionsOptional
    } yield startSpanWithRemoteParent(
      headerTracingId,
      SpanContext.create(
        TraceId.fromLowerBase16(traceId),
        SpanId.fromLowerBase16(spanId),
        TraceOptions.fromLowerBase16(traceOptions, 0),
        Tracestate.builder().build()
      )
    )
    span
  }

}
