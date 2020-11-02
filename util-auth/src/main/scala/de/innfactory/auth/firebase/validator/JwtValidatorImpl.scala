package de.innfactory.auth.firebase.validator

import com.google.firebase.auth.{FirebaseAuth, FirebaseToken}
import com.nimbusds.jwt.proc.BadJWTException

import scala.util.{Failure, Success, Try}

class JwtValidatorImpl  extends JwtValidator {

    override def validate(jwtToken: JwtToken): Either[BadJWTException, FirebaseToken] = {
      val decodedToken = Try(FirebaseAuth.getInstance.verifyIdToken(jwtToken.content))
      decodedToken match {
        case Success(dt) => Right(dt)
        case Failure(f)  => Left(new BadJWTException(f.getMessage, f))
      }
    }
  
}
