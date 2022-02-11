package de.innfactory.play.slick.enhanced.utils.filteroptions

import slick.lifted.Rep

case class StringOption[T](selector: T => Rep[String], queryIdentifier: String) extends StringFilterOptions[T]

case class OptionStringOption[T](selector: T => Rep[Option[String]], queryIdentifier: String)
    extends OptionStringFilterOptions[T]

case class IntOption[T](selector: T => Rep[Int], queryIdentifier: String) extends IntFilterOptions[T]

case class LongOption[T](selector: T => Rep[Long], queryIdentifier: String) extends LongFilterOptions[T]

case class DoubleOption[T](selector: T => Rep[Double], queryIdentifier: String) extends DoubleFilterOptions[T]

case class SequenceOption[T](selector: T => Rep[String], queryIdentifier: String) extends SeqFilterOptions[T]

case class BooleanOption[T](selector: T => Rep[Boolean], queryIdentifier: String) extends BooleanFilterOptions[T]
