package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.http.*
import tyrian.Html.*
import cats.effect.IO
import tyrian.cmds.Logger
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.domain.auth.*

final case class SignUpPage(
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    firstName: String = "",
    lastName: String = "",
    company: String = "",
    status: Option[Page.Status] = None
) extends Page {
  import SignUpPage.*

  override def initCmd: Cmd[IO, Page.Msg] =
    Cmd.None

  override def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg]) = msg match {
    case UpdateEmail(e)            => (this.copy(email = e), Cmd.None)
    case UpdatePassword(p)         => (this.copy(password = p), Cmd.None)
    case UpdateConfirmPassword(cp) => (this.copy(confirmPassword = cp), Cmd.None)
    case UpdateFirstName(v)        => (this.copy(firstName = v), Cmd.None)
    case UpdateLastName(v)         => (this.copy(lastName = v), Cmd.None)
    case UpdateCompany(v)          => (this.copy(company = v), Cmd.None)
    case AttemptSignUp =>
      if (!email.matches(Constants.emailRegex))
        (setErrorStatus("Email is invalid"), Cmd.None)
      else if (password.isEmpty)
        (setErrorStatus("Please enter a password"), Cmd.None)
      else if (password != confirmPassword)
        (setErrorStatus("Password fields do not match"), Cmd.None)
      else
        (
          this,
          Commands.signup(
            NewUserInfo(
              email,
              password,
              Option(firstName).filter(_.nonEmpty),
              Option(lastName).filter(_.nonEmpty),
              Option(company).filter(_.nonEmpty)
            )
          )
        )
    case SignUpError(message) =>
      (setErrorStatus(message), Cmd.None)
    case SignUpSuccess(message) =>
      (setSuccessStatus(message), Cmd.None)
    case _ => (this, Cmd.None)
  }

  override def view(): Html[Page.Msg] =
    div(`class` := "form-section")(
      div(`class` := "top-section")(
        h1("Sign Up")
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
        // 6 inputs
        renderInput("Email", "email", "text", true, UpdateEmail(_)),
        renderInput("Password", "password", "password", true, UpdatePassword(_)),
        renderInput("Confirm password", "cPassword", "password", true, UpdateConfirmPassword(_)),
        renderInput("First Name", "firstName", "text", false, UpdateFirstName(_)),
        renderInput("Last Name", "lastName", "text", false, UpdateLastName(_)),
        renderInput("Company", "company", "text", false, UpdateCompany(_)),
        // button
        button(`type` := "button", onClick(AttemptSignUp))("Sign Up")
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

object SignUpPage {
  trait Msg                                                 extends Page.Msg
  case class UpdateEmail(email: String)                     extends Msg
  case class UpdatePassword(password: String)               extends Msg
  case class UpdateConfirmPassword(confirmPassword: String) extends Msg
  case class UpdateFirstName(firstName: String)             extends Msg
  case class UpdateLastName(lastName: String)               extends Msg
  case class UpdateCompany(company: String)                 extends Msg
  // actions
  case object AttemptSignUp extends Msg
  case object NoOp          extends Msg
  // statuses
  case class SignUpError(message: String)   extends Msg
  case class SignUpSuccess(message: String) extends Msg

  object Endpoints {
    val signup = new Endpoint[Msg] {
      val location = Constants.Endpoints.signup
      val method   = Method.Post
      val onSuccess: Response => Msg = response =>
        response.status match {
          case Status(201, _) =>
            SignUpSuccess("Success! Log in now.")
          case Status(s, _) if s >= 400 && s < 500 =>
            val json   = response.body
            val parsed = parse(json).flatMap(_.hcursor.get[String]("error"))
            parsed match {
              case Left(e)  => SignUpError(s"Error: ${e.getMessage}")
              case Right(e) => SignUpError(e)
            }
        }

      val onError: HttpError => Msg =
        e => SignUpError(e.toString)
    }
  }

  object Commands {
    def signup(newUserInfo: NewUserInfo): Cmd[IO, Msg] =
      Endpoints.signup.call(newUserInfo)
  }
}
