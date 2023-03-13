package com.rockthejvm.jobsboard.components

import tyrian.*
import tyrian.Html.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.components.*
import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.pages.*

object Header {

  // public API
  def view() =
    div(`class` := "container-fluid p-0")(
      div(`class` := "jvm-nav")(
        div(`class` := "container")(
          nav(`class` := "navbar navbar-expand-lg navbar-light JVM-nav")(
            div(`class` := "container")(
              renderLogo(),
              button(
                `class` := "navbar-toggler",
                `type`  := "button",
                attribute("data-bs-toggle", "collapse"),
                attribute("data-bs-target", "#navbarNav"),
                attribute("aria-controls", "navbarNav"),
                attribute("aria-expanded", "false"),
                attribute("aria-label", "Toggle navigation")
              )(
                span(`class` := "navbar-toggler-icon")()
              ),
              div(`class` := "collapse navbar-collapse", id := "navbarNav")(
                ul(
                  `class` := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3"
                )(
                  renderNavLinks()
                )
              )
            )
          )
        )
      )
    )

  // private API
  @js.native
  @JSImport("/static/img/fiery-lava 128x128.png", JSImport.Default)
  private val logoImage: String = js.native

  private def renderLogo() =
    a(
      href    := "/",
      `class` := "navbar-brand",
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

  private def renderNavLinks(): List[Html[App.Msg]] = {
    val constantLinks = List(
      renderSimpleNavLink("Jobs", Page.Urls.JOBS),
      renderSimpleNavLink("Post Job", Page.Urls.POST_JOB)
    )

    val unauthedLinks = List(
      renderSimpleNavLink("Login", Page.Urls.LOGIN),
      renderSimpleNavLink("Sign Up", Page.Urls.SIGNUP)
    )

    val authedLinks = List(
      renderSimpleNavLink("Profile", Page.Urls.PROFILE),
      renderNavLink("Log Out", Page.Urls.HASH)(_ => Session.Logout)
    )

    constantLinks ++ (
      if (Session.isActive) authedLinks
      else unauthedLinks
    )
  }

  private def renderSimpleNavLink(text: String, location: String) =
    renderNavLink(text, location)(Router.ChangeLocation(_))

  private def renderNavLink(text: String, location: String)(location2msg: String => App.Msg) =
    li(`class` := "nav-item")(
      Anchors.renderNavLink(text, location, "nav-link jvm-item")(location2msg)
    )
}
