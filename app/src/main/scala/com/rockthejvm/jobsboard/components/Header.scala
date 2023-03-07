package com.rockthejvm.jobsboard.components

import tyrian.*
import tyrian.Html.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.pages.*

object Header {

  // public API
  def view() =
    div(`class` := "header-container")(
      renderLogo(),
      div(`class` := "header-nav")(
        ul(`class` := "header-links")(
          renderNavLink("Jobs", Page.Urls.JOBS),
          renderNavLink("Login", Page.Urls.LOGIN),
          renderNavLink("Sign Up", Page.Urls.SIGNUP)
        )
      )
    )

  // private API
  @js.native
  @JSImport("/static/img/fiery-lava 128x128.png", JSImport.Default)
  private val logoImage: String = js.native

  private def renderLogo() =
    a(
      href := "/",
      onEvent(
        "click",
        e => {
          e.preventDefault()
          Router.ChangeLocation("/")
        }
      )
    )(
      img(
        `class` := "home-logo",
        src     := logoImage,
        alt     := "Rock the JVM"
      )
    )

  private def renderNavLink(text: String, location: String) =
    li(`class` := "nav-item")(
      a(
        href    := location,
        `class` := "nav-link",
        onEvent(
          "click",
          e => {
            e.preventDefault() // native JS - prevent reloading the page
            Router.ChangeLocation(location)
          }
        )
      )(text)
    )
}
