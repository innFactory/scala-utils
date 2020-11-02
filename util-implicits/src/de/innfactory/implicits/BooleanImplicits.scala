package de.innfactory.implicits

object BooleanImplicits {
  implicit class EnhancedBoolean[T](value: Boolean) {
    def toResult(leftResult: T): Either[T, Boolean] =
      if (value)
        Right(true)
      else
        Left(leftResult)
  }
}