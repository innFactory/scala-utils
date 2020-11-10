package de.innfactory.auth.firebase.implicits

import java.util.Base64

import de.innfactory.auth.firebase.validator.JwtToken
import play.api.Environment
import play.libs.Json

object JwtImplicits {

  implicit class StringToJwtEnhancer(authHeader: String) {
    def toJwtToken =
      authHeader match {
        case token: String if token.startsWith("Bearer") =>
          JwtToken(token.splitAt(7)._2)
        case token                                       => JwtToken(token)
      }
  }

  implicit class JwtTokenEnhancer(jwtToken: JwtToken) {
    def getUserId(implicit environment: Environment): Option[String] =
      if (environment.mode.toString == "Test")
        Some(jwtToken.content)
      else
        try {
          val tokenContent = jwtToken.content.split('.')(1)
          val decoded      = new String(Base64.getDecoder.decode(tokenContent))
          val userId       = Json.parse(decoded).get("user_id").asText()
          Some(userId)
        } catch {
          case _: Exception => None
        }

  }
}
