package com.rockthejvm.jobsboard

import tyrian.*
import tyrian.Html.*
import cats.effect.*
import scala.scalajs.js.annotation.*
import org.scalajs.dom.window
import scala.concurrent.duration.*

import core.*
import components.*

object App {
  type Msg = Router.Msg

  case class Model(router: Router)
}

@JSExportTopLevel("RockTheJvmApp")
class App extends TyrianApp[App.Msg, App.Model] {
  import App.*

  override def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) = {
    val (router, cmd) = Router.startAt(window.location.pathname)
    (Model(router), cmd)
  }

  override def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.make( // listener for browser history changes
      "urlChange",
      model.router.history.state.discrete
        .map(_.get)
        .map(newLocation => Router.ChangeLocation(newLocation, true))
    )

  override def update(model: Model): Msg => (Model, Cmd[IO, Msg]) = { case msg: Router.Msg =>
    val (newRouter, cmd) = model.router.update(msg)
    (model.copy(router = newRouter), cmd)
  }

  // view triggered whenever model changes
  override def view(model: Model): Html[Msg] =
    div(
      Header.view(),
      div(s"You are now at: ${model.router.location}")
    )

}
