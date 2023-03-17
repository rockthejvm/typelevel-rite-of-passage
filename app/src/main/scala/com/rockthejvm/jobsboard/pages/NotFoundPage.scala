package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*

final case class NotFoundPage() extends Page {
  override def initCmd: Cmd[IO, App.Msg] =
    Cmd.None

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) =
    (this, Cmd.None)

  override def view(): Html[App.Msg] =
    div(`class` := "row")(
      div(`class` := "col-md-5 p-0")(
        // left
        div(`class` := "logo")(
          img(src   := Constants.logoImage)
        )
      ),
      div(
        `class` := "col-md-7"
      )(
        div(`class` := "form-section")(
          div(`class` := "top-section")(
            h1(span("🤦‍♂️ Ouch!")),
            div("This page doesn't exist. You lost or something?")
          )
        )
      )
    )
}
