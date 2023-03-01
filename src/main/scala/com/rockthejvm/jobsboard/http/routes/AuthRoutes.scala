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

import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.http.responses.*
import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.domain.auth.*
import com.rockthejvm.jobsboard.domain.security.*
import com.rockthejvm.jobsboard.domain.user.*

import scala.language.implicitConversions

class AuthRoutes[F[_]: Concurrent: Logger: SecuredHandler] private (
    auth: Auth[F],
    authenticator: Authenticator[F]
) extends HttpValidationDsl[F] {

  // POST /auth/login { LoginInfo } => 200 OK with Authorization: Bearer {jwt}
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    req.validate[LoginInfo] { loginInfo =>
      val maybeJwtToken = for {
        maybeUser  <- auth.login(loginInfo.email, loginInfo.password)
        _          <- Logger[F].info(s"User logging in: ${loginInfo.email}")
        maybeToken <- maybeUser.traverse(user => authenticator.create(user.email))
      } yield maybeToken

      maybeJwtToken.map {
        case Some(token) => authenticator.embed(Response(Status.Ok), token)
        case None        => Response(Status.Unauthorized)
      }
    }
  }

  // POST /auth/users { NewUserInfo } => 201 Created or BadRequest
  private val createUserRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      req.validate[NewUserInfo] { newUserInfo =>
        for {
          maybeNewUser <- auth.signUp(newUserInfo)
          resp <- maybeNewUser match {
            case Some(user) => Created(user.email)
            case None       => BadRequest(s"User with email ${newUserInfo.email} already exists.")
          }
        } yield resp
      }
  }

  // PUT /auth/users/password { NewPasswordInfo } { Authorization: Bearer {jwt} } => 200 OK
  private val changePasswordRoute: AuthRoute[F] = {
    case req @ PUT -> Root / "users" / "password" asAuthed user =>
      req.request.validate[NewPasswordInfo] { newPasswordInfo =>
        for {
          maybeUserOrError <- auth.changePassword(user.email, newPasswordInfo)
          resp <- maybeUserOrError match {
            case Right(Some(_)) => Ok()
            case Right(None)    => NotFound(FailureResponse(s"User ${user.email} not found."))
            case Left(_)        => Forbidden()
          }
        } yield resp
      }
  }

  // POST /auth/logout { Authorization: Bearer {jwt} } => 200 OK
  private val logoutRoute: AuthRoute[F] = { case req @ POST -> Root / "logout" asAuthed _ =>
    val token = req.authenticator
    for {
      _    <- authenticator.discard(token)
      resp <- Ok()
    } yield resp
  }

  // DELETE /auth/users/daniel@rockthejvm.com
  private val deleteUserRoute: AuthRoute[F] = {
    case req @ DELETE -> Root / "users" / email asAuthed user =>
      auth.delete(email).flatMap {
        case true  => Ok()
        case false => NotFound()
      }
  }

  val unauthedRoutes = loginRoute <+> createUserRoute
  val authedRoutes = SecuredHandler[F].liftService(
    changePasswordRoute.restrictedTo(allRoles) |+|
      logoutRoute.restrictedTo(allRoles) |+|
      deleteUserRoute.restrictedTo(adminOnly)
  )

  val routes = Router(
    "/auth" -> (unauthedRoutes <+> authedRoutes)
  )
}

/*
  - need a CAPABILITY, instead of intermediate values (use DI in that case)
  - instantiated ONCE in the entire app
 */
object AuthRoutes {
  def apply[F[_]: Concurrent: Logger: SecuredHandler](
      auth: Auth[F],
      authenticator: Authenticator[F]
  ) =
    new AuthRoutes[F](auth, authenticator)
}
