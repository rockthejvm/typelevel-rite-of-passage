package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import cats.effect.IO
import org.scalajs.dom.*

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.core.*
import scala.concurrent.duration.FiniteDuration
import org.scalajs.dom.HTMLInputElement

abstract class FormPage(title: String, status: Option[Page.Status]) extends Page {

  // abstract API
  protected def renderFormContent(): List[Html[App.Msg]] // for every page to override

  // public API
  override def initCmd: Cmd[cats.effect.IO, App.Msg] =
    clearForm()

  override def view(): Html[App.Msg] =
    renderForm()

  // protected API
  protected def renderForm(): Html[App.Msg] =
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
        // right
        div(`class` := "form-section")(
          div(`class` := "top-section")(
            h1(span(title)),
            maybeRenderErrors()
          ),
          form(
            name    := "signin",
            `class` := "form",
            id      := "form",
            onEvent(
              "submit",
              e => {
                e.preventDefault()
                App.NoOp
              }
            )
          )(
            renderFormContent()
          )
        )
      )
    )

  protected def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      onChange: String => App.Msg
  ) =
    div(`class` := "row")(
      div(`class` := "col-md-12")(
        div(`class` := "form-input")(
          label(`for` := uid, `class` := "form-label")(
            if (isRequired) span("*") else span(),
            text(name)
          ),
          input(`type` := kind, `class` := "form-control", id := uid, onInput(onChange))
        )
      )
    )

  protected def renderToggle(
      name: String,
      uid: String,
      isRequired: Boolean,
      onChange: String => App.Msg
  ) =
    div(`class` := "row")(
      div(`class` := "col-md-12 job")(
        div(`class` := "form-check form-switch")(
          label(`for` := uid, `class` := "form-check-label")(
            if (isRequired) span("*") else span(),
            text(name)
          ),
          input(`type` := "checkbox", `class` := "form-check-input", id := uid, onInput(onChange))
        )
      )
    )

  protected def renderImageUploadInput(
      name: String,
      uid: String,
      imgSrc: Option[String],
      onChange: Option[File] => App.Msg
  ) =
    div(`class` := "row")(
      div(`class` := "col-md-12")(
        div(`class` := "form-input")(
          label(`for` := uid, `class` := "form-label")(name),
          input(
            `type`  := "file",
            `class` := "form-control",
            id      := uid,
            accept  := "image/*",
            onEvent(
              "change",
              e => {
                val imageInput = e.target.asInstanceOf[HTMLInputElement]
                val fileList   = imageInput.files // FileList
                if (fileList.length > 0)
                  onChange(Some(fileList(0)))
                else
                  onChange(None)
              }
            )
          )
        )
      )
    )

  protected def renderTextArea(
      name: String,
      uid: String,
      isRequired: Boolean,
      onChange: String => App.Msg
  ) =
    div(`class` := "row")(
      div(`class` := "col-md-12")(
        div(`class` := "form-input")(
          label(`for` := name, `class` := "form-label")(
            if (isRequired) span("*") else span(),
            text(name)
          ),
          textarea(`class` := "form-control", id := uid, onInput(onChange))("")
        )
      )
    )

  // private
  // UI
  private def maybeRenderErrors() =
    status
      .filter(s => s.kind == Page.StatusKind.ERROR && s.message.nonEmpty)
      .map(s => div(`class` := "form-errors")(s.message))
      .getOrElse(div())

  // logic
  private def clearForm() =
    Cmd.Run[IO, Unit, App.Msg] {
      def effect: IO[Option[HTMLFormElement]] = for {
        maybeForm <- IO(Option(document.getElementById("form").asInstanceOf[HTMLFormElement]))
        finalForm <-
          if (maybeForm.isEmpty) IO.sleep(FiniteDuration(100, "millis")) *> effect
          else IO(maybeForm)
      } yield finalForm

      effect.map(_.foreach(_.reset()))
    }(_ => App.NoOp)
}
