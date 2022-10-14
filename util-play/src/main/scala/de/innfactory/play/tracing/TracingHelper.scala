package de.innfactory.play.tracing

import de.innfactory.play.smithy4play.HttpHeaders
import de.innfactory.play.tracing.GoogleTracingIdentifier.XTRACINGID
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter

import java.lang
import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters.IterableHasAsJava

object TracingHelper {

  def traceWithParent[T](traceString: String, parent: Span, processToTrace: Span => Future[T])(implicit
    ec: ExecutionContext
  ): Future[T] = {
    val childSpan = TracerProvider
      .getTracer()
      .spanBuilder(traceString)
      .setParent(Context.current.`with`(parent))
      .startSpan
    for {
      result <- processToTrace(childSpan)
      _       = childSpan.end()
    } yield result
  }

  def generateSpanFromRemoteSpan[A](
    headers: HttpHeaders
  ): Option[Span] = {
    val map              = scala.collection.mutable.Map.from[String, String](headers.rc.map(m => (m._1, m._2.head)))
    val extractedContext = GlobalOpenTelemetry.getPropagators.getTextMapPropagator
      .extract(
        Context.current(),
        map,
        new TextMapGetter[scala.collection.mutable.Map[String, String]] {
          override def keys(carrier: mutable.Map[String, String]): lang.Iterable[String] = carrier.keys.asJava
          override def get(carrier: mutable.Map[String, String], key: String): String    = carrier.get(key).orNull
        }
      )
    extractedContext.makeCurrent()
    Some(
      TracerProvider
        .getTracer()
        .spanBuilder(headers.getHeader(XTRACINGID).getOrElse("generateSpanFromRemoteSpan"))
        .setParent(extractedContext)
        .startSpan()
    )
  }
}
