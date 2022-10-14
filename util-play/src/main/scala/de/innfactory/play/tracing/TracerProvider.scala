package de.innfactory.play.tracing

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.opentelemetry.trace.{ TraceConfiguration, TraceExporter }
import com.google.devtools.cloudtrace.v2.{ AttributeValue, TruncatableString }
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporters.inmemory.InMemorySpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.{ BatchSpanProcessor, SpanExporter }
import play.api.inject.ApplicationLifecycle

import java.io.InputStream
import scala.concurrent.{ ExecutionContext, Promise }
import scala.jdk.CollectionConverters.MapHasAsJava

object TracerProvider {
  private var tracer: Option[Tracer] = None

  def getTracer(): Tracer =
    tracer.getOrElse(
      throw new InstantiationException("Tracer not configured! Call TracerProvider.configure on application startup")
    )

  def configure(
    instrumenationScopeName: String,
    projectId: String,
    credentialsFile: Option[String]
  )(implicit lifeCycle: ApplicationLifecycle, ec: ExecutionContext): Unit = {

    val traceExporter     = createExporter(projectId, credentialsFile)
    val sdkTracerProvider = createSdkTracerProvider(traceExporter)

    val openTelemetry: OpenTelemetry = OpenTelemetrySdk.builder
      .setTracerProvider(sdkTracerProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance))
      .buildAndRegisterGlobal

    tracer = Some(openTelemetry.getTracer(instrumenationScopeName))

    lifeCycle.addStopHook { () =>
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

  private def createExporter(projectId: String, credentialsFile: Option[String]): SpanExporter =
    credentialsFile match {
      case Some(file) =>
        val serviceAccount: InputStream    = getClass.getClassLoader.getResourceAsStream(file)
        val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
        val traceConfig                    = TraceConfiguration
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
      case None       => InMemorySpanExporter.create()
    }

  private def createSdkTracerProvider(spanExporter: SpanExporter) =
    SdkTracerProvider.builder
      .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
      .build()
}
