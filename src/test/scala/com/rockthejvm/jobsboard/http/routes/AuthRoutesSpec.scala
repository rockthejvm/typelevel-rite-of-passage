package com.rockthejvm.jobsboard.http.routes

import cats.effect.*
import cats.data.*
import cats.implicits.*
import org.typelevel.ci.CIStringSyntax
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.implicits.*
import org.http4s.headers.Authorization
import org.scalatest.freespec.AsyncFreeSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tsec.authentication.IdentityStore
import tsec.authentication.JWTAuthenticator
import tsec.mac.jca.HMACSHA256
import tsec.jws.mac.JWTMac

import scala.concurrent.duration.*

import com.rockthejvm.jobsboard.domain.user.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.fixtures.*

class AuthRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with UserFixture {

  ///////////////////////////////////////////////////////////////////////////////
  // prep
  ///////////////////////////////////////////////////////////////////////////////

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

  val mockedAuth: Auth[IO] = new Auth[IO] {
    def login(email: String, password: String): IO[Option[JwtToken]] =
      if (email == danielEmail && password == danielPassword)
        mockedAuthenticator.create(danielEmail).map(Some(_))
      else IO.pure(None)

    def signUp(newUserInfo: NewUserInfo): IO[Option[User]] =
      if (newUserInfo.email == riccardoEmail)
        IO.pure(Some(Riccardo))
      else
        IO.pure(None)

    def changePassword(
        email: String,
        newPasswordInfo: NewPasswordInfo
    ): IO[Either[String, Option[User]]] =
      if (email == danielEmail)
        if (newPasswordInfo.oldPassword == danielPassword)
          IO.pure(Right(Some(Daniel)))
        else
          IO.pure(Left("Invalid password"))
      else
        IO.pure(Right(None))

    override def delete(email: String): IO[Boolean] = IO.pure(true)
    def authenticator: Authenticator[IO]            = mockedAuthenticator
  }

  extension (r: Request[IO])
    def withBearerToken(a: JwtToken): Request[IO] =
      r.putHeaders {
        val jwtString = JWTMac.toEncodedString[IO, HMACSHA256](a.jwt)
        // Authorization: Bearer {jwt}
        Authorization(Credentials.Token(AuthScheme.Bearer, jwtString))
      }

  given logger: Logger[IO]       = Slf4jLogger.getLogger[IO]
  val authRoutes: HttpRoutes[IO] = AuthRoutes[IO](mockedAuth).routes

  ///////////////////////////////////////////////////////////////////////////////
  // tests
  ///////////////////////////////////////////////////////////////////////////////

  "AuthRoutes" - {
    "should return a 401 - unauthorized if login fails" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginInfo(danielEmail, "wrongpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - OK + a JWT if login is successful" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/login")
            .withEntity(LoginInfo(danielEmail, danielPassword))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
        response.headers.get(ci"Authorization") shouldBe defined
      }
    }

    "should return a 400 - Bad Request if the user to create already exists" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(NewUserDaniel)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.BadRequest
      }
    }

    "should return a 201 - Created if the user creation succeeds" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/users")
            .withEntity(NewUserRiccardo)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Created
      }
    }

    "should return a 200 - OK if logging out with a valid JWT" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/logout")
            .withBearerToken(jwtToken)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 401 - Unauthorized if logging out without a valid JWT" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/auth/logout")
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 404 - Not Found if changing password for a user that doesn't exist" in {
      for {
        jwtToken <- mockedAuthenticator.create(riccardoEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo(riccardoPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.NotFound
      }
    }

    "should return a 403 - Forbidden if old password is incorrect" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo("wrongpassword", "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Forbidden
      }
    }

    "should return a 401 - Unauthorized if changing password without a JWT" in {
      for {
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withEntity(NewPasswordInfo(danielPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - OK if changing password for a user with valid JWT and password" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/auth/users/password")
            .withBearerToken(jwtToken)
            .withEntity(NewPasswordInfo(danielPassword, "newpassword"))
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }

    "should return a 401 - Unauthorized if a non-admin tries to delete a user" in {
      for {
        jwtToken <- mockedAuthenticator.create(riccardoEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/auth/users/daniel@rockthejvm.com")
            .withBearerToken(jwtToken)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Unauthorized
      }
    }

    "should return a 200 - Ok if an admin tries to delete a user" in {
      for {
        jwtToken <- mockedAuthenticator.create(danielEmail)
        response <- authRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/auth/users/daniel@rockthejvm.com")
            .withBearerToken(jwtToken)
        )
      } yield {
        // assertions here
        response.status shouldBe Status.Ok
      }
    }
  }
}
