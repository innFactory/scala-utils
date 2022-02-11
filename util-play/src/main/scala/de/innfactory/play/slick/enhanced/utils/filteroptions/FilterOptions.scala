package de.innfactory.play.slick.enhanced.utils.filteroptions

import slick.lifted.Rep

trait FilterOptions[E, V] {
  def selector: E => Rep[V]

  val queryIdentifier: String

  def getFromQueryString(params: Map[String, Seq[String]]): Option[FilterOptions[E, V]]

  def atLeasOneFilterOptionApplicable: Boolean

  def optionsToString: String
}
