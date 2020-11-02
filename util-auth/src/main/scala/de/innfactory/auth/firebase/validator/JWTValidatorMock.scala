package de.innfactory.auth.firebase.validator

import com.nimbusds.jwt.proc.BadJWTException
import scala.util.{Failure, Success, Try}

class JWTValidatorMock extends JwtValidator {
  override def validate(jwtToken: JwtToken): Either[BadJWTException, String] = {
    val decodedToken: Try[String] = {
      if (jwtToken.content == "VerifiedUser")
        Success("VerifiedUser")
      else
        Failure(new BadJWTException("failure"))
    }
    decodedToken match {
      case Success(dt) => Right(dt)
      case Failure(f)  => Left(new BadJWTException(f.getMessage, f))
    }
  }
}


