package com.rockthejvm.jobsboard.components

import tyrian.*
import tyrian.Html.*
import cats.effect.IO

trait Component[Msg, +Model] {
  // send a command upon instantiating
  def initCmd: Cmd[IO, Msg]
  // update
  def update(msg: Msg): (Model, Cmd[IO, Msg])
  // render
  def view(): Html[Msg]
}
