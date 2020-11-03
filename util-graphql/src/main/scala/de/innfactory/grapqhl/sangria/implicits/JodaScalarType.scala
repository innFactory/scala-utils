package de.innfactory.grapqhl.sangria.implicits

import org.joda.time.DateTime
import sangria.ast.StringValue
import sangria.schema.ScalarType
import sangria.validation.Violation

object JodaScalarType {

  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error during parsing DateTime"
  }

  implicit val GraphQLDateTime: ScalarType[DateTime] = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case s: StringValue => Right(DateTime.parse(s.value))
      case _              => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: StringValue => Right(DateTime.parse(s.value))
      case _              => Left(DateTimeCoerceViolation)
    }
  )

}
