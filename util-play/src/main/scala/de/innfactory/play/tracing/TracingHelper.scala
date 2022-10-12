package de.innfactory.play.tracing

import com.google.cloud.opentelemetry.metric.{ GoogleCloudMetricExporter, MetricConfiguration }
import com.google.cloud.opentelemetry.trace.{ TraceConfiguration, TraceExporter }
import com.typesafe.config.Config
import de.innfactory.play.smithy4play.HttpHeaders
import de.innfactory.play.tracing.GoogleTracingIdentifier.{
  XTRACINGID,
  X_INTERNAL_SPANID,
  X_INTERNAL_TRACEID,
  X_INTERNAL_TRACEOPTIONS
}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.api.trace.{ Span, SpanContext, SpanId, TraceFlags, TraceId, TraceState, Tracer }
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.PeriodicMetricReader
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor

import scala.concurrent.{ ExecutionContext, Future }

object TracingHelper {

  def traceWithParent[T](traceString: String, parent: Span)(implicit
    logContext: LogContext,
    config: Config,
    ec: ExecutionContext
  ): (Span => Future[T]) => Future[T] =
    (process: Span => Future[T]) => {
      val childSpan = logContext.getTracer
        .spanBuilder(traceString)
        .setParent(Context.current.`with`(parent))
        .startSpan
      for {
        result <- process(childSpan)
        _       = childSpan.end()
      } yield result
    }

  def generateSpanFromRemoteSpan[A](
    headers: HttpHeaders
  )(implicit logContext: LogContext, config: Config): Option[Span] = {
    val headerTracingIdOptional = headers.getHeader(XTRACINGID)
    val spanIdOptional          = headers.getHeader(X_INTERNAL_SPANID)
    val traceIdOptional         = headers.getHeader(X_INTERNAL_TRACEID)
    val traceOptionsOptional    = headers.getHeader(X_INTERNAL_TRACEOPTIONS)

    for {
      headerTracingId <- headerTracingIdOptional
      spanId          <- spanIdOptional
      traceId         <- traceIdOptional
      traceOptions    <- traceOptionsOptional
    } yield logContext.getTracer
      .spanBuilder(headerTracingId)
      .setParent(
        Context
          .current()
          .`with`(
            Span.wrap(
              SpanContext.createFromRemoteParent(
                TraceId.fromBytes(traceId.getBytes),
                SpanId.fromBytes(spanId.getBytes),
                TraceFlags.fromHex(traceOptions, 0),
                TraceState.builder().build()
              )
            )
          )
      )
      .startSpan()
  }

  def createTracer(
    projectId: String,
    instrumentationScopeName: String,
    instrumentationScopeVersion: String
  ): Tracer = {
    val traceExporter: TraceExporter              =
      TraceExporter.createWithConfiguration(TraceConfiguration.builder().setProjectId(projectId).build());
    val metricExporter: GoogleCloudMetricExporter =
      GoogleCloudMetricExporter.createWithConfiguration(
        MetricConfiguration.builder().setProjectId(projectId).build()
      )

    val sdkTracerProvider: SdkTracerProvider = SdkTracerProvider.builder
      .addSpanProcessor(BatchSpanProcessor.builder(traceExporter).build)
      .build

    val sdkMeterProvider: SdkMeterProvider = SdkMeterProvider.builder
      .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build)
      .build

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder
      .setTracerProvider(sdkTracerProvider)
      .setMeterProvider(sdkMeterProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance))
      .buildAndRegisterGlobal

    openTelemetry.getTracer(instrumentationScopeName, instrumentationScopeVersion)
  }
}
