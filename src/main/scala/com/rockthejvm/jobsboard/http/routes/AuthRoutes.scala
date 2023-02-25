package com.rockthejvm.jobsboard.http.routes

import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*

import cats.effect.*
import cats.implicits.*
import org.typelevel.log4cats.Logger
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.Status
import tsec.authentication.asAuthed
import tsec.authentication.SecuredRequestHandler
import tsec.authentication.TSecAuthService

import com.rockthejvm.jobsboard.http.responses.*
import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.domain.user.*

class AuthRoutes[F[_]: Concurrent: Logger] private (auth: Auth[F]) extends HttpValidationDsl[F] {

  private val authenticator = auth.authenticator
  private val securedHandler: SecuredRequestHandler[F, String, User, JwtToken] =
    SecuredRequestHandler(authenticator)

  // POST /auth/login { LoginInfo } => 200 OK with Authorization: Bearer {jwt}
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    val maybeJwtToken = for {
      loginInfo  <- req.as[LoginInfo]
      maybeToken <- auth.login(loginInfo.email, loginInfo.password)
      _          <- Logger[F].info(s"User logging in: ${loginInfo.email}")
    } yield maybeToken

    maybeJwtToken.map {
      case Some(token) => authenticator.embed(Response(Status.Ok), token)
      case None        => Response(Status.Unauthorized)
    }
  }

  // POST /auth/users { NewUserInfo } => 201 Created or BadRequest
  private val createUserRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      for {
        newUserInfo  <- req.as[NewUserInfo]
        maybeNewUser <- auth.signUp(newUserInfo)
        resp <- maybeNewUser match {
          case Some(user) => Created(user.email)
          case None       => BadRequest(s"User with email ${newUserInfo.email} already exists.")
        }
      } yield resp
  }

  // PUT /auth/users/password { NewPasswordInfo } { Authorization: Bearer {jwt} } => 200 OK
  private val changePasswordRoute: AuthRoute[F] = {
    case req @ PUT -> Root / "users" / "password" asAuthed user =>
      for {
        newPasswordInfo  <- req.request.as[NewPasswordInfo]
        maybeUserOrError <- auth.changePassword(user.email, newPasswordInfo)
        resp <- maybeUserOrError match {
          case Right(Some(_)) => Ok()
          case Right(None)    => NotFound(FailureResponse(s"User ${user.email} not found."))
          case Left(_)        => Forbidden()
        }
      } yield resp
  }

  // POST /auth/logout { Authorization: Bearer {jwt} } => 200 OK
  private val logoutRoute: AuthRoute[F] = { case req @ POST -> Root / "logout" asAuthed _ =>
    val token = req.authenticator
    for {
      _    <- authenticator.discard(token)
      resp <- Ok()
    } yield resp
  }

  val unauthedRoutes = loginRoute <+> createUserRoute
  val authedRoutes = securedHandler.liftService(
    TSecAuthService(changePasswordRoute.orElse(logoutRoute))
  )

  val routes = Router(
    "/auth" -> (unauthedRoutes <+> authedRoutes)
  )
}

object AuthRoutes {
  def apply[F[_]: Concurrent: Logger](auth: Auth[F]) =
    new AuthRoutes[F](auth)
}
