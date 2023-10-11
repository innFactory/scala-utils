package de.innfactory.play.tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.MetricExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.SpanExporter
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future, Promise}

object OpentelemetryProvider {
  private var tracer: Option[Tracer] = None
  private var meter: Option[Meter]   = None

  def getTracer(): Tracer =
    tracer.getOrElse(
      throw new InstantiationException("Tracer not configured! Call TracerProvider.configure on application startup")
    )

  def getMeter(): Meter =
    meter.getOrElse(
      throw new InstantiationException("Meter not configured! Call TracerProvider.configure on application startup")
    )

  def configureMock()(implicit lifeCycle: ApplicationLifecycle, ec: ExecutionContext): Unit = {
    if (tracer.isDefined && meter.isDefined) return
    val sdkTracerProvider = SdkTracerProvider.builder().build()
    val sdkMeterProvider  = SdkMeterProvider.builder().build()

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder
      .setTracerProvider(sdkTracerProvider)
      .setMeterProvider(sdkMeterProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance))
      .buildAndRegisterGlobal

    tracer = Some(
      openTelemetry.getTracer(
        "mock"
      )
    )
    meter = Some(
      openTelemetry.getMeter(
        "mock"
      )
    )
  }

  def configure(
    instrumenationScopeName: String,
    spanExporter: SpanExporter,
    sdkTracerProvider: SdkTracerProvider,
    metricExporter: MetricExporter,
    sdkMeterProvider: SdkMeterProvider,
    contextPropagators: ContextPropagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance),
  )(implicit lifeCycle: ApplicationLifecycle, ec: ExecutionContext): Unit = {

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder
      .setTracerProvider(sdkTracerProvider)
      .setMeterProvider(sdkMeterProvider)
      .setPropagators(contextPropagators)
      .buildAndRegisterGlobal

    tracer = Some(openTelemetry.getTracer(instrumenationScopeName))
    meter = Some(openTelemetry.getMeter(instrumenationScopeName))

    lifeCycle.addStopHook { () =>
      Future.sequence(
        Seq(
          createStopHookTracer(sdkTracerProvider, spanExporter),
          createStopHookMeter(sdkMeterProvider, metricExporter)
        )
      )
    }
  }

  private def createStopHookMeter(sdkMeterProvider: SdkMeterProvider, metricExporter: MetricExporter) = {
    val promise = Promise[Unit]()
    sdkMeterProvider
      .forceFlush()
      .whenComplete { () =>
        metricExporter.flush().whenComplete { () =>
          promise.success()
        }
      }
    promise.future
  }

  private def createStopHookTracer(sdkTracerProvider: SdkTracerProvider, traceExporter: SpanExporter) = {
    val promise = Promise[Unit]()
    sdkTracerProvider
      .forceFlush()
      .whenComplete { () =>
        traceExporter.flush().whenComplete { () =>
          promise.success()
        }
      }
    promise.future
  }
}
