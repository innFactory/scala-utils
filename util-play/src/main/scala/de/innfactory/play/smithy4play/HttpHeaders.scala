package de.innfactory.play.smithy4play

case class HttpHeaders(rc: Map[String, Seq[String]]) {

  private val lowerCaseMap = rc.map(t => t._1.toLowerCase -> t._2)

  private val authorizationHeader = "authorization"

  private def accessHeader(name: String): Option[String] =
    lowerCaseMap.get(name.toLowerCase).flatMap(_.headOption)

  def authorization: Option[String] = accessHeader(authorizationHeader)

  def authAsJwt: Option[JWTToken] = authorization.map(JWTToken)

  def getHeader(name: String): Option[String] = accessHeader(name)

}
