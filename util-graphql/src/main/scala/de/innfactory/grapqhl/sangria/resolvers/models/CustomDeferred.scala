package de.innfactory.familotel.cms.graphql.resolvers.models

import de.innfactory.familotel.cms.models.api.{ BookingCombined, ForecastAggregate, OccupancyRate }
import sangria.execution.deferred.Deferred

object CustomDeferred {

  sealed trait Matchable {
    protected val typeName: String = this.getClass.getSimpleName
    def getName                    = typeName
  }

  case class BookingsByHotelsDeferred(hotelId: String, filters: Option[String])
      extends Deferred[Seq[BookingCombined]]
      with Matchable

  case class BookingsByHotelIdsDeferred(hotelIds: Seq[Long], filters: Option[String])
      extends Deferred[Seq[BookingCombined]]
      with Matchable

  case class OccupancyByHotelIdsDeferred(hotelIds: Seq[Long], fromYear: Int, fromMonth: Int, toYear: Int, toMonth: Int)
      extends Deferred[Seq[OccupancyRate]]
      with Matchable

  case class ForecastByHotelIdsDeferred(hotelIds: Seq[Long], fromYear: Int, fromWeek: Int, toYear: Int, toWeek: Int)
      extends Deferred[Seq[ForecastAggregate]]
      with Matchable

  case class Unsupported() extends Matchable
}
