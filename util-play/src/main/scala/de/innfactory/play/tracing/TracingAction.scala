package de.innfactory.play.tracing

import de.innfactory.play.tracing.GoogleTracingIdentifier._
import io.opencensus.scala.Tracing.{startSpan, startSpanWithRemoteParent, traceWithParent}
import io.opencensus.trace.{Span, SpanContext, SpanId, TraceId, TraceOptions, Tracestate}
import play.api.Environment
import play.api.mvc.{ActionBuilder, AnyContent, BodyParsers, Request, Result, WrappedRequest}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class TracingAction @Inject() (
                                val parser: BodyParsers.Default,
                                implicit val environment: Environment
                              )(implicit val executionContext: ExecutionContext) {
  def apply(traceString: String): TraceActionBuilder = new TraceActionBuilder(traceString, parser)
}

class TraceActionBuilder(spanString: String, val parser: BodyParsers.Default)(implicit
                                                                                               val executionContext: ExecutionContext
) extends ActionBuilder[RequestWithTrace, AnyContent] {

  def finishSpan[A](request: TraceRequest[A], result: Result, parentSpan: Span): Result = {
    request.traceSpan.end()
    parentSpan.end()
    result
  }

  override def invokeBlock[A](request: Request[A], block: RequestWithTrace[A] => Future[Result]): Future[Result] = {
    val optionalSpan: Option[_root_.io.opencensus.trace.Span] = generateSpanFromRemoteSpan(request)
    val span                                                  = optionalSpan.getOrElse(startSpan(spanString))
    traceWithParent(spanString, span) { spanChild =>
      createRequestWithSpanAndInvokeBlock(request, block, spanChild)
    }
  }

  private def createRequestWithSpanAndInvokeBlock[A](
                                                      request: Request[A],
                                                      block: RequestWithTrace[A] => Future[Result],
                                                      span: Span
                                                    ): Future[Result] = {
    val requestWithTrace = new RequestWithTrace(span, request)
    block(requestWithTrace).map { r =>
      finishSpan(requestWithTrace, r, span)
    }
  }

  private def generateSpanFromRemoteSpan[A](request: Request[A]): Option[Span] = {
    val headerTracingIdOptional = request.headers.get(XTRACINGID)
    val spanIdOptional          = request.headers.get(X_INTERNAL_SPANID)
    val traceIdOptional         = request.headers.get(X_INTERNAL_TRACEID)
    val traceOptionsOptional    = request.headers.get(X_INTERNAL_TRACEOPTIONS)

    val span = for {
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
