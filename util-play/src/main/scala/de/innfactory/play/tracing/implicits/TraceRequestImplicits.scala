package de.innfactory.play.tracing.implicits

import de.innfactory.play.tracing.TraceRequest
import play.api.mvc.AnyContent

import scala.concurrent.ExecutionContext

object TraceRequestImplicits {
  implicit class EnhancedTraceRequest(requestWithTrace: TraceRequest[AnyContent])(implicit ec: ExecutionContext) {
    def extractHeaders: Seq[(String, String)] = {
      Seq(
        ("X-Tracing-ID", requestWithTrace.request.headers.get("X-Tracing-ID").get),
        ("X-Internal-SpanId", requestWithTrace.traceSpan.getContext.getSpanId.toLowerBase16),
        ("X-Internal-TraceId", requestWithTrace.traceSpan.getContext.getTraceId.toLowerBase16),
        ("X-Internal-TraceOption", requestWithTrace.traceSpan.getContext.getTraceOptions.toLowerBase16)
      )
    }
  }
}
