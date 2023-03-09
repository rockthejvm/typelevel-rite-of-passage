package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import cats.effect.IO
import io.circe.generic.auto.*

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.domain.auth.*
import tyrian.cmds.Logger

final case class LoginPage(
    email: String = "",
    password: String = "",
    status: Option[Page.Status] = None
) extends Page {
  import LoginPage.*

  override def initCmd: Cmd[IO, App.Msg] =
    Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case UpdateEmail(e)    => (this.copy(email = e), Cmd.None)
    case UpdatePassword(p) => (this.copy(password = p), Cmd.None)
    case AttemptLogin =>
      if (!email.matches(Constants.emailRegex))
        (setErrorStatus("Invalid email"), Cmd.None)
      else if (password.isEmpty)
        (setErrorStatus("Please enter a password"), Cmd.None)
      else (this, Commands.login(LoginInfo(email, password)))
    case LoginError(error) =>
      (setErrorStatus(error), Cmd.None)
    case LoginSuccess(token) =>
      (setSuccessStatus("Success!"), Cmd.Emit(Session.SetToken(email, token, isNewUser = true)))
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    div(`class` := "form-section")(
      div(`class` := "top-section")(
        h1("Log In")
      ),
      form(
        name    := "signin",
        `class` := "form",
        onEvent(
          "submit",
          e => {
            e.preventDefault()
            NoOp
          }
        )
      )(
        renderInput("Email", "email", "text", true, UpdateEmail(_)),
        renderInput("Password", "password", "password", true, UpdatePassword(_)),
        button(`type` := "button", onClick(AttemptLogin))("Log In")
      ),
      status.map(s => div(s.message)).getOrElse(div())
    )

  /////////////////////////////////////////////////////////////////////////
  // private
  /////////////////////////////////////////////////////////////////////////

  // UI
  private def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      onChange: String => Msg
  ) =
    div(`class` := "form-input")(
      label(`for` := name, `class` := "form-label")(
        if (isRequired) span("*") else span(),
        text(name)
      ),
      input(`type` := kind, `class` := "form-control", id := uid, onInput(onChange))
    )

  // util
  def setErrorStatus(message: String): Page =
    this.copy(status = Some(Page.Status(message, Page.StatusKind.ERROR)))
  def setSuccessStatus(message: String): Page =
    this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
}

object LoginPage {
  trait Msg                                   extends App.Msg
  case class UpdateEmail(email: String)       extends Msg
  case class UpdatePassword(password: String) extends Msg
  // actions
  case object AttemptLogin extends Msg
  case object NoOp         extends Msg
  // results
  case class LoginError(error: String)   extends Msg
  case class LoginSuccess(token: String) extends Msg

  object Endpoints {
    val login = new Endpoint[Msg] {
      override val location: String = Constants.Endpoints.login
      override val method: Method   = Method.Post
      override val onError: HttpError => Msg =
        e => LoginError(e.toString)
      override val onSuccess: Response => Msg = response => {
        val maybeToken = response.headers.get("authorization")
        maybeToken match {
          case Some(token) => LoginSuccess(token)
          case None        => LoginError("Invalid username or password")
        }
      }
    }
  }

  object Commands {
    def login(loginInfo: LoginInfo) =
      Endpoints.login.call(loginInfo)
  }
}
