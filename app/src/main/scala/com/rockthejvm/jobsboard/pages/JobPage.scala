package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.Html.*
import tyrian.http.*
import cats.effect.IO
import io.circe.generic.auto.*

import laika.api.*
import laika.format.*

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.components.*
import com.rockthejvm.jobsboard.domain.job.*

import scala.scalajs.*
import scala.scalajs.js.* // js.native
import scala.scalajs.js.annotation.*

@js.native
@JSGlobal()
class Moment extends js.Object {
  def fromNow(): String = js.native
  def format(): String  = js.native
  // surface out any JS function as a new Scala method in this class
}

@js.native
@JSImport("moment", JSImport.Default) // run a JS import statement
object MomentLib extends js.Object {
  def unix(seconds: Long): Moment = js.native
}

final case class JobPage(
    id: String,
    maybeJob: Option[Job] = None,
    status: Page.Status = Page.Status.LOADING
) extends Page {
  import JobPage.*

  override def initCmd: Cmd[IO, App.Msg] =
    Commands.getJob(id)

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case SetError(e) =>
      (setErrorStatus(e), Cmd.None)
    case SetJob(job) =>
      (setSuccessStatus("Success").copy(maybeJob = Some(job)), Cmd.None)
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    maybeJob match {
      case Some(job) => renderJobPage(job)
      case None      => renderNoJobPage()
    }

  // private

  // UI

  private def renderJobPage(job: Job) =
    div(`class` := "container-fluid the-rock")(
      div(`class` := "row jvm-jobs-details-top-card")(
        div(`class` := "col-md-12 p-0")(
          div(`class` := "jvm-jobs-details-card-profile-img")(
            img(
              `class` := "img-fluid",
              src     := job.jobInfo.image.getOrElse(""),
              alt     := job.jobInfo.title
            )
          ),
          div(`class` := "jvm-jobs-details-card-profile-title")(
            h1(s"${job.jobInfo.company} - ${job.jobInfo.title}"),
            div(`class` := "jvm-jobs-details-card-profile-job-details-company-and-location")(
              JobComponents.renderJobSummary(job)
            )
          ),
          div(`class` := "jvm-jobs-details-card-apply-now-btn")(
            a(href := job.jobInfo.externalUrl, target := "blank")(
              button(`type` := "button", `class` := "btn btn-warning")("Apply now")
            ),
            p(
              MomentLib.unix(job.date / 1000).fromNow()
            )
          )
        )
      ),
      div(`class` := "container-fluid")(
        div(`class` := "container")(
          div(`class` := "markdown-body overview-section")(
            renderJobDescription(job)
          )
        ),
        div(`class` := "container")(
          div(`class` := "rok-last")(
            div(`class` := "row")(
              div(`class` := "col-md-6 col-sm-6 col-6")(
                span(`class` := "rock-apply")("Apply for this job.")
              ),
              div(`class` := "col-md-6 col-sm-6 col-6")(
                a(href := job.jobInfo.externalUrl, target := "blank")(
                  button(`type` := "button", `class` := "rock-apply-btn")("Apply now")
                )
              )
            )
          )
        )
      )
    )

  private def renderJobDescription(job: Job) = {
    val descriptionHtml = markdownTransformer.transform(job.jobInfo.description) match {
      case Left(e) =>
        """
          Dammit. 
          Had an error showing Markdown for this job description. 
          Just hit the apply button (that should still work) - also let them know about the problem!
        """
      case Right(html) => html
    }
    div(`class` := "job-description")().innerHtml(descriptionHtml)
  }

  private def renderNoJobPage() =
    div(`class` := "container-fluid the-rock")(
      div(`class` := "row jvm-jobs-details-top-card")(
        status.kind match {
          case Page.StatusKind.LOADING =>
            h1("Loading...")
          case Page.StatusKind.ERROR =>
            h1("Ouch! This job doesn't exist.")
          case Page.StatusKind.SUCCESS =>
            h1("Something's fishy. Server is healthy but no job...")
        }
      )
    )

  // logic
  val markdownTransformer = Transformer
    .from(Markdown)
    .to(HTML)
    .build

  def setErrorStatus(message: String) =
    this.copy(status = Page.Status(message, Page.StatusKind.ERROR))
  def setSuccessStatus(message: String) =
    this.copy(status = Page.Status(message, Page.StatusKind.SUCCESS))
}

object JobPage {
  trait Msg                          extends App.Msg
  case class SetError(error: String) extends Msg
  case class SetJob(job: Job)        extends Msg

  object Endpoints {
    def getJob(id: String) = new Endpoint[Msg] {
      override val location: String          = Constants.endpoints.jobs + s"/$id"
      override val method: Method            = Method.Get
      override val onError: HttpError => Msg = e => SetError(e.toString)
      override val onResponse: Response => Msg =
        Endpoint.onResponse[Job, Msg](SetJob(_), SetError(_))
    }
  }

  object Commands {
    def getJob(id: String) =
      Endpoints.getJob(id).call()
  }
}
