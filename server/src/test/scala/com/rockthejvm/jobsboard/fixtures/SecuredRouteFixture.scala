package com.rockthejvm.jobsboard.fixtures

import cats.data.*
import cats.effect.*
import org.http4s.*
import org.http4s.headers.*
import tsec.authentication.IdentityStore
import tsec.authentication.JWTAuthenticator
import tsec.mac.jca.HMACSHA256
import tsec.jws.mac.JWTMac

import scala.concurrent.duration.*

import com.rockthejvm.jobsboard.domain.user.*
import com.rockthejvm.jobsboard.domain.security.*
import tsec.authentication.SecuredRequestHandler

trait SecuredRouteFixture extends UserFixture {
  val mockedAuthenticator: Authenticator[IO] = {
    // key for hashing
    val key = HMACSHA256.unsafeGenerateKey
    // identity store to retrieve users
    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      if (email == danielEmail) OptionT.pure(Daniel)
      else if (email == riccardoEmail) OptionT.pure(Riccardo)
      else OptionT.none[IO, User]
    // jwt authenticator
    JWTAuthenticator.unbacked.inBearerToken(
      1.day,   // expiration of tokens
      None,    // max idle time (optional)
      idStore, // identity store
      key      // hash key
    )
  }

  extension (r: Request[IO])
    def withBearerToken(a: JwtToken): Request[IO] =
      r.putHeaders {
        val jwtString = JWTMac.toEncodedString[IO, HMACSHA256](a.jwt)
        // Authorization: Bearer {jwt}
        Authorization(Credentials.Token(AuthScheme.Bearer, jwtString))
      }

  given securedHandler: SecuredHandler[IO] = SecuredRequestHandler(mockedAuthenticator)
}
