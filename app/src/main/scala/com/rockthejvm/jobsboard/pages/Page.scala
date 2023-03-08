package com.rockthejvm.jobsboard.pages

import tyrian.*
import cats.effect.IO

object Page {
  trait Msg

  enum StatusKind {
    case SUCCESS, ERROR, LOADING
  }
  final case class Status(message: String, kind: StatusKind)

  object Urls {
    val LOGIN            = "/login"
    val SIGNUP           = "/signup"
    val FORGOT_PASSWORD  = "/forgotpassword"
    val RECOVER_PASSWORD = "/recoverpassword"
    val JOBS             = "/jobs"
    val EMPTY            = ""
    val HOME             = "/"
  }

  import Urls.*
  def get(location: String) = location match {
    case `LOGIN`                   => LoginPage()
    case `SIGNUP`                  => SignUpPage()
    case `FORGOT_PASSWORD`         => ForgotPasswordPage()
    case `RECOVER_PASSWORD`        => RecoverPasswordPage()
    case `EMPTY` | `HOME` | `JOBS` => JobListPage()
    case s"/jobs/$id"              => JobPage(id)
    case _                         => NotFoundPage()
  }
}

abstract class Page {
  // API

  // send a command upon instantiating
  def initCmd: Cmd[IO, Page.Msg]
  // update
  def update(msg: Page.Msg): (Page, Cmd[IO, Page.Msg])
  // render
  def view(): Html[Page.Msg]
}
