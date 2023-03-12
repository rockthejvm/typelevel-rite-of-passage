package com.rockthejvm.jobsboard.pages

import tyrian.*
import cats.effect.IO

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.components.*

object Page {
  trait Msg

  enum StatusKind {
    case SUCCESS, ERROR, LOADING
  }
  final case class Status(message: String, kind: StatusKind)

  object Urls {
    val LOGIN           = "/login"
    val SIGNUP          = "/signup"
    val FORGOT_PASSWORD = "/forgotpassword"
    val RESET_PASSWORD  = "/resetpassword"
    val PROFILE         = "/profile"
    val POST_JOB        = "/postjob"
    val JOBS            = "/jobs"
    val EMPTY           = ""
    val HOME            = "/"
    val HASH            = "#"
  }

  import Urls.*
  def get(location: String) = location match {
    case `LOGIN`                   => LoginPage()
    case `SIGNUP`                  => SignUpPage()
    case `FORGOT_PASSWORD`         => ForgotPasswordPage()
    case `RESET_PASSWORD`          => ResetPasswordPage()
    case `PROFILE`                 => ProfilePage()
    case `POST_JOB`                => PostJobPage()
    case `EMPTY` | `HOME` | `JOBS` => JobListPage()
    case s"/jobs/$id"              => JobPage(id)
    case _                         => NotFoundPage()
  }
}

abstract class Page extends Component[App.Msg, Page]
