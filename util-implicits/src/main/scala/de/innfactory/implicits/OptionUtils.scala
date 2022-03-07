package de.innfactory.implicits

object OptionUtils {
  implicit class EnhancedOption[T](value: Option[T]) {
    def getOrElseOld(oldOption: Option[T]): Option[T] =
      value match {
        case Some(of) => Some(of)
        case None     => oldOption
      }

    def toEither[L](leftResult: L): Either[L, T] =
      value match {
        case Some(v) => Right(v)
        case None    => Left(leftResult)
      }

    def toInverseEither[L](leftResult: L): Either[L, String] =
      value match {
        case Some(_) => Left(leftResult)
        case None    => Right("")
      }

      def toResult[L](leftResult: L): Either[L, T] =
        if (value.isDefined) Right(value.get) else Left(leftResult)
  }
}
