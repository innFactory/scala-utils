package de.innfactory.play.smithy4play

import cats.data.{ EitherT, Kleisli }
import de.innfactory.play.tracing.TracingHelper.{ generateSpanFromRemoteSpan, traceWithParent }
import de.innfactory.smithy4play.{ ContextRouteError, RouteResult, RoutingContext }
import io.opentelemetry.api.trace.{ Span, Tracer }

import scala.concurrent.{ ExecutionContext, Future }

abstract class AbstractBaseController[E, UC, RC <: ContextWithHeaders] {

  protected type ApplicationRouteResult[O] = EitherT[Future, E, O]

  def errorHandler(e: E): ContextRouteError

  def createRequestContextFromRoutingContext(r: RoutingContext): RC

  def AuthAction: Kleisli[ApplicationRouteResult, RC, UC]

  object EndpointAction {
    def apply[I, O](r: I => ApplicationRouteResult[O]): Kleisli[ApplicationRouteResult, I, O] =
      Kleisli.apply(r)
  }

  def TransformRoutingContextToApplicationRoutingContext(implicit
    ec: ExecutionContext
  ): Kleisli[ApplicationRouteResult, RoutingContext, RC] =
    EndpointAction { r: RoutingContext =>
      EitherT.rightT[Future, E](createRequestContextFromRoutingContext(r))
    }

  case class TracingAction(traceString: String)(implicit ec: ExecutionContext, tracer: Tracer) {
    def andThen[O](
      result: Kleisli[ApplicationRouteResult, RC, O]
    ): Kleisli[ApplicationRouteResult, RC, O] = {

      def finishSpan[A](parentSpan: Span, childSpan: Span, result: O): O = {
        childSpan.end()
        parentSpan.end()
        result
      }

      EndpointAction { r: RC =>
        val optionalSpan: Option[Span] = generateSpanFromRemoteSpan(r.httpHeaders)
        val span                       = optionalSpan.getOrElse(tracer.spanBuilder(traceString).startSpan())
        val traceActionResult          = traceWithParent(traceString, span)(tracer, ec) { spanChild =>
          result(r).map { r =>
            finishSpan(span, spanChild, r)
          }.value
        }
        EitherT(traceActionResult)
      }

    }
  }

  // (RoutingContext => RoutingToRequestContextKleisli => RequestContext => TracingAction => additionalAction)

  object Endpoint {

    def withAuth(implicit ec: ExecutionContext, logContext: LogContext, tracer: Tracer): EndpointAction[UC] =
      addAdditionalAction(AuthAction)

    def withAction[O](
      additionalAction: Kleisli[ApplicationRouteResult, RC, O]
    )(implicit ec: ExecutionContext, logContext: LogContext, tracer: Tracer): EndpointAction[O] = addAdditionalAction(
      additionalAction
    )

    def execute[O](
      f: RC => ApplicationRouteResult[O]
    )(implicit ec: ExecutionContext, logContext: LogContext, tracer: Tracer): EndpointAction[O] =
      addAdditionalAction(Kleisli.apply(f))

    private def addAdditionalAction[O](
      additionalAction: Kleisli[ApplicationRouteResult, RC, O]
    )(implicit ec: ExecutionContext, logContext: LogContext, tracer: Tracer): EndpointAction[O] = {
      val owningMethodName                                                          = Thread.currentThread.getStackTrace()(2).getMethodName
      val tracingActionWithAdditionalAction: Kleisli[ApplicationRouteResult, RC, O] =
        TracingAction(
          logContext.className + " " + owningMethodName
        ).andThen(additionalAction)
      val chained                                                                   = TransformRoutingContextToApplicationRoutingContext andThen tracingActionWithAdditionalAction
      new EndpointAction[O](chained)
    }

  }

  class EndpointAction[O](initialAction: Kleisli[ApplicationRouteResult, RoutingContext, O])(implicit
    ec: ExecutionContext
  ) {
    def withAction[NO](chainableAction: Kleisli[ApplicationRouteResult, O, NO]): EndpointAction[NO] = {
      val chainedKleisli = initialAction andThen chainableAction
      new EndpointAction(chainedKleisli)
    }

    def execute[NO](f: O => ApplicationRouteResult[NO])(implicit ec: ExecutionContext): EndpointAction[NO] =
      new EndpointAction(initialAction andThen Kleisli.apply(f))

    def complete[OUT](implicit t: O => OUT): Kleisli[RouteResult, RoutingContext, OUT] = initialAction.map(t).mapF {
      applicationResult => applicationResult.leftMap(errorHandler)
    }
  }

}
