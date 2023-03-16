package com.rockthejvm.jobsboard.core

import tyrian.*
import cats.effect.IO
import fs2.dom.History

import org.scalajs.dom.window

import com.rockthejvm.jobsboard.*

case class Router private (location: String, history: History[IO, String]) {
  import Router.*

  def update(msg: Msg): (Router, Cmd[IO, Msg]) = msg match {
    case ChangeLocation(newLocation, browserTrigerred) =>
      if (location == newLocation) (this, Cmd.None)
      else {
        val historyCmd =
          if (browserTrigerred) Cmd.None // browser action, no need to push location on history
          else goto(newLocation)         // manual action, need to push location
        (this.copy(location = newLocation), historyCmd)
      }
    case ExternalRedirect(location) =>
      window.location.href = maybeCleanUrl(location)
      (this, Cmd.None)
  }

  def goto[M](location: String): Cmd[IO, M] =
    Cmd.SideEffect[IO] {
      history.pushState(location, location)
    }

  // private
  private def maybeCleanUrl(url: String) =
    if (url.startsWith("\""))
      url.substring(1, url.length() - 1)
    else url // keep it simple
}

object Router {
  trait Msg                                                                      extends App.Msg
  case class ChangeLocation(location: String, browserTriggered: Boolean = false) extends Msg
  case class ExternalRedirect(location: String)                                  extends Msg

  def startAt[M](initialLocation: String): (Router, Cmd[IO, M]) = {
    val router = Router(initialLocation, History[IO, String])
    (router, router.goto(initialLocation))
  }
}
