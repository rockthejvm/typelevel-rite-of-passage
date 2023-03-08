package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.Html.*
import cats.effect.IO

import com.rockthejvm.jobsboard.*

final case class JobListPage() extends Page {
  override def initCmd: Cmd[IO, App.Msg] =
    Cmd.None // TODO
  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) =
    (this, Cmd.None) // TODO
  override def view(): Html[App.Msg] =
    div("Job List page - TODO")
}
