package de.innfactory.play.tracing

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.opentelemetry.metric.{ GoogleCloudMetricExporter, MetricConfiguration }
import com.google.cloud.opentelemetry.trace.{ TraceConfiguration, TraceExporter }
import com.google.devtools.cloudtrace.v2.{ AttributeValue, TruncatableString }
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.logging.{ LoggingMetricExporter, LoggingSpanExporter }
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.{ MetricExporter, PeriodicMetricReader }
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.{ BatchSpanProcessor, SpanExporter }
import play.api.inject.ApplicationLifecycle

import java.io.InputStream
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.jdk.CollectionConverters.MapHasAsJava

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

  def configure(
    instrumenationScopeName: String,
    projectId: String,
    credentialsFile: Option[String]
  )(implicit lifeCycle: ApplicationLifecycle, ec: ExecutionContext): Unit = {

    val traceExporter     = createTraceExporter(projectId, credentialsFile)
    val meterExporter     = createMeterExporter(projectId, credentialsFile)
    val sdkTracerProvider = createSdkTracerProvider(traceExporter)
    val sdkMeterProvider  = createSdkMeterProvider(meterExporter)

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder
      .setTracerProvider(sdkTracerProvider)
      .setMeterProvider(sdkMeterProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance))
      .buildAndRegisterGlobal

    tracer = Some(openTelemetry.getTracer(instrumenationScopeName))
    meter = Some(openTelemetry.getMeter(instrumenationScopeName))

    lifeCycle.addStopHook { () =>
      Future.sequence(
        Seq(
          createStopHookTracer(sdkTracerProvider, traceExporter),
          createStopHookMeter(sdkMeterProvider, meterExporter)
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

  private def createGcpCredentials(credentialsFile: Option[String]) = credentialsFile.map { file =>
    val serviceAccount: InputStream = getClass.getClassLoader.getResourceAsStream(file)
    GoogleCredentials.fromStream(serviceAccount)
  }

  private def createTraceExporter(projectId: String, credentialsFile: Option[String]): SpanExporter =
    createGcpCredentials(credentialsFile) match {
      case Some(credentials) =>
        val traceConfig = TraceConfiguration
          .builder()
          .setProjectId(projectId)
          .setCredentials(credentials)
          .setFixedAttributes(
            Map(
              (
                "/component",
                AttributeValue
                  .newBuilder()
                  .setStringValue(TruncatableString.newBuilder().setValue("PlayServer").build())
                  .build()
              )
            ).asJava
          )
        TraceExporter.createWithConfiguration(traceConfig.build())
      case None              => LoggingSpanExporter.create()
    }

  private def createMeterExporter(projectId: String, credentialsFile: Option[String]): MetricExporter =
    createGcpCredentials(credentialsFile) match {
      case Some(credentials) =>
        val meterConfig = MetricConfiguration
          .builder()
          .setProjectId(projectId)
          .setCredentials(credentials)
        GoogleCloudMetricExporter.createWithConfiguration(meterConfig.build())
      case None              => LoggingMetricExporter.create()
    }

  private def createSdkTracerProvider(spanExporter: SpanExporter) =
    SdkTracerProvider.builder
      .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
      .build()

  private def createSdkMeterProvider(metricExporter: MetricExporter) =
    SdkMeterProvider
      .builder()
      .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build())
      .build();
}
