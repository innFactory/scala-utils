package de.innfactory.play.tracing

import io.opencensus.scala.Tracing.{ startSpanWithRemoteParent, traceWithParent }
import io.opencensus.trace.{ Span, SpanContext, SpanId, TraceId, TraceOptions, Tracestate }
import play.api.Environment
import play.api.mvc.{ ActionBuilder, AnyContent, BodyParsers, Request, Result, WrappedRequest }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class RequestWithTrace[A](val traceSpan: Span, val request: Request[A])
    extends WrappedRequest[A](request)
    with TraceRequest[A]

protected class TraceActionBuilder(spanString: String, val parser: BodyParsers.Default)(implicit
  val executionContext: ExecutionContext
) extends ActionBuilder[RequestWithTrace, AnyContent] {

  def finishSpan[A](request: TraceRequest[A], result: Result, parentSpan: Span): Result = {
    request.traceSpan.end()
    parentSpan.end()
    result
  }

  override def invokeBlock[A](request: Request[A], block: RequestWithTrace[A] => Future[Result]): Future[Result] = {
    val headerTracingId = request.headers.get("X-Tracing-ID").get
    val spanId          = request.headers.get("X-Internal-SpanId").get
    val traceId         = request.headers.get("X-Internal-TraceId").get
    val traceOptions    = request.headers.get("X-Internal-TraceOption").get

    val span = startSpanWithRemoteParent(
      headerTracingId,
      SpanContext.create(
        TraceId.fromLowerBase16(traceId),
        SpanId.fromLowerBase16(spanId),
        TraceOptions.fromLowerBase16(traceOptions, 0),
        Tracestate.builder().build()
      )
    )

    traceWithParent(spanString, span) { spanChild =>
      val requestWithTrace = new RequestWithTrace(spanChild, request)
      block(requestWithTrace).map { r =>
        finishSpan(requestWithTrace, r, span)
      }
    }

  }
}

class TracingAction @Inject() (
  val parser: BodyParsers.Default,
  implicit val environment: Environment
)(implicit val executionContext: ExecutionContext) {
  def apply(traceString: String): TraceActionBuilder = new TraceActionBuilder(traceString, parser)
}
