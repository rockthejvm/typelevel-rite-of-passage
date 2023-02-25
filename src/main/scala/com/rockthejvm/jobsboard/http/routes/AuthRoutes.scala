package com.rockthejvm.jobsboard.http.routes

import cats.effect.*
import cats.implicits.*
import org.typelevel.log4cats.Logger
import org.http4s.server.Router
import org.http4s.HttpRoutes

import com.rockthejvm.jobsboard.http.validation.syntax.*
import com.rockthejvm.jobsboard.core.*

class AuthRoutes[F[_]: Concurrent: Logger] private (auth: Auth[F]) extends HttpValidationDsl[F] {

  // POST /auth/login { LoginInfo } => 200 OK with Authorization: Bearer {jwt}
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "login" =>
    Ok("TODO")
  }

  // POST /auth/users { NewUserInfo } => 201 Created or BadRequest
  private val createUserRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "users" =>
    Ok("TODO")
  }

  // PUT /auth/users/password { NewPasswordInfo } { Authorization: Bearer {jwt} } => 200 OK
  private val changePasswordRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case PUT -> Root / "users" / "password" =>
      Ok("TODO")
  }

  // POST /auth/logout { Authorization: Bearer {jwt} } => 200 OK
  private val logoutRoute: HttpRoutes[F] = HttpRoutes.of[F] { case POST -> Root / "logout" =>
    Ok("TODO")
  }

  val routes = Router(
    "/auth" -> (loginRoute <+> createUserRoute <+> changePasswordRoute <+> logoutRoute)
  )
}

object AuthRoutes {
  def apply[F[_]: Concurrent: Logger](auth: Auth[F]) =
    new AuthRoutes[F](auth)
}
