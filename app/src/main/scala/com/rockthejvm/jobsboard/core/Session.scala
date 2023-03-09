package com.rockthejvm.jobsboard.core

import tyrian.*
import tyrian.http.*
import cats.effect.IO
import tyrian.cmds.Logger
import org.scalajs.dom.document
import scala.scalajs.js.Date

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.pages.Page

final case class Session(email: Option[String] = None, token: Option[String] = None) {
  import Session.*

  def update(msg: Msg): (Session, Cmd[IO, App.Msg]) = msg match {
    case SetToken(e, t, isNewUser) =>
      val cookieCmd = Commands.setAllSessionCookies(e, t, isNewUser)
      val routingCmd =
        if (isNewUser) Cmd.Emit(Router.ChangeLocation(Page.Urls.HOME))
        else Cmd.None
      (this.copy(email = Some(e), token = Some(t)), cookieCmd |+| routingCmd)
    case Logout =>
      val cmd = token.map(_ => Commands.logout).getOrElse(Cmd.None)
      (this, cmd)
    case LogoutSuccess =>
      (
        this.copy(email = None, token = None),
        Commands.clearAllSessionCookies() |+| Cmd.Emit(Router.ChangeLocation(Page.Urls.HOME))
      )
  }

  def initCmd: Cmd[IO, Msg] = {
    val maybeCommand = for {
      email <- getCookie(Constants.cookies.email)
      token <- getCookie(Constants.cookies.token)
    } yield Cmd.Emit(SetToken(email, token, isNewUser = false))

    maybeCommand.getOrElse(Cmd.None)
  }
}

object Session {
  trait Msg                                                                     extends App.Msg
  case class SetToken(email: String, token: String, isNewUser: Boolean = false) extends Msg
  case object Logout                                                            extends Msg
  case object LogoutSuccess                                                     extends Msg
  case object LogoutFailure                                                     extends Msg

  def isActive =
    getUserToken().nonEmpty

  def getUserToken() =
    getCookie(Constants.cookies.token)

  object Endpoints {
    val logout = new Endpoint[Msg] {
      val location                   = Constants.Endpoints.logout
      val method                     = Method.Post
      val onSuccess: Response => Msg = _ => LogoutSuccess
      val onError: HttpError => Msg  = _ => LogoutFailure
    }
  }

  object Commands {
    def logout: Cmd[IO, Msg] =
      Endpoints.logout.callAuthorized()

    def setSessionCookie(name: String, value: String, isFresh: Boolean = false): Cmd[IO, Msg] =
      Cmd.SideEffect[IO] {
        if (getCookie(name).isEmpty || isFresh)
          document.cookie =
            s"$name=$value;expires=${new Date(Date.now() + Constants.cookies.duration)};path=/"
      }

    def setAllSessionCookies(email: String, token: String, isFresh: Boolean = false): Cmd[IO, Msg] =
      setSessionCookie(Constants.cookies.email, email, isFresh) |+|
        setSessionCookie(Constants.cookies.token, token, isFresh)

    def clearSessionCookie(name: String): Cmd[IO, Msg] =
      Cmd.SideEffect[IO] {
        document.cookie = s"$name=;expires=${new Date(0)};path=/"
      }

    def clearAllSessionCookies(): Cmd[IO, Msg] =
      clearSessionCookie(Constants.cookies.email) |+|
        clearSessionCookie(Constants.cookies.token)
  }

  private def getCookie(name: String): Option[String] =
    document.cookie
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$name="))
      .map(_.split("="))
      .map(_(1))
}
