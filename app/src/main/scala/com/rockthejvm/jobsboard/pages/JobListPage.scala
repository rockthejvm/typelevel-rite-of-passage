package com.rockthejvm.jobsboard.pages

import tyrian.*
import tyrian.http.*
import tyrian.Html.*
import cats.effect.IO
import io.circe.parser.*
import io.circe.generic.auto.*

import com.rockthejvm.jobsboard.domain.job.*
import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*

final case class JobListPage(
    jobs: List[Job] = List(),
    canLoadMore: Boolean = true,
    status: Option[Page.Status] = Some(Page.Status("Loading", Page.StatusKind.LOADING))
) extends Page {
  import JobListPage.*

  override def initCmd: Cmd[IO, App.Msg] =
    Commands.getJobs()

  override def update(msg: App.Msg): (Page, Cmd[IO, App.Msg]) = msg match {
    case AddJobs(list, clm) =>
      (setSuccessStatus("Loaded").copy(jobs = this.jobs ++ list, canLoadMore = clm), Cmd.None)
    case SetErrorStatus(e) =>
      (setErrorStatus(e), Cmd.None)
    case LoadMoreJobs =>
      (this, Commands.getJobs(offset = jobs.length))
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    div(`class` := "jobs-container")(
      jobs.map(renderJob) ++ maybeRenderLoadMore
    )

  /////////////////////////////////////////////////////////////////////////
  // private
  /////////////////////////////////////////////////////////////////////////

  // UI
  private def renderJob(job: Job) =
    div(`class` := "job-card")(
      div(`class` := "job-card-img")(
        img(
          `class` := "job-logo",
          src     := job.jobInfo.image.getOrElse(""),
          alt     := job.jobInfo.title
        )
      ),
      div(`class` := "job-card-content")(
        h4(s"${job.jobInfo.company} - ${job.jobInfo.title}")
      ),
      div(`class` := "job-card-apply")(
        a(href := job.jobInfo.externalUrl, target := "blank")("Apply")
      )
    )

  private def maybeRenderLoadMore: Option[Html[App.Msg]] = status.map { s =>
    div(`class` := "load-more-action")(
      s match {
        case Page.Status(_, Page.StatusKind.LOADING) => div("Loading...")
        case Page.Status(e, Page.StatusKind.ERROR)   => div(e)
        case Page.Status(_, Page.StatusKind.SUCCESS) =>
          if (canLoadMore)
            button(`type` := "button", onClick(LoadMoreJobs))("Load more")
          else
            div("All jobs loaded")
      }
    )
  }

  // util
  def setErrorStatus(message: String) =
    this.copy(status = Some(Page.Status(message, Page.StatusKind.ERROR)))
  def setSuccessStatus(message: String) =
    this.copy(status = Some(Page.Status(message, Page.StatusKind.SUCCESS)))
}

object JobListPage {
  trait Msg extends App.Msg
  // state
  case class SetErrorStatus(e: String)                      extends Msg
  case class AddJobs(list: List[Job], canLoadMore: Boolean) extends Msg
  // action
  case object LoadMoreJobs extends Msg

  object Endpoints {
    def getJobs(limit: Int = Constants.defaultPageSize, offset: Int = 0) = new Endpoint[Msg] {
      override val location: String = Constants.endpoints.jobs + s"?limit=$limit&offset=$offset"
      override val method: Method   = Method.Post
      override val onError: HttpError => Msg = e => SetErrorStatus(e.toString)
      override val onResponse: Response => Msg = response =>
        response.status match {
          case Status(s, _) if s >= 200 && s < 300 =>
            val json   = response.body
            val parsed = parse(json).flatMap(_.as[List[Job]])
            parsed match {
              case Left(parsingError) => SetErrorStatus(s"Parsing error: $parsingError")
              case Right(list)        => AddJobs(list, canLoadMore = offset == 0 || !list.isEmpty)
            }
          case Status(code, message) if code >= 400 && code < 600 =>
            SetErrorStatus(s"Error: $message")
        }
    }
  }

  object Commands {
    def getJobs(
        filter: JobFilter = JobFilter(),
        limit: Int = Constants.defaultPageSize,
        offset: Int = 0
    ): Cmd[IO, Msg] =
      Endpoints.getJobs(limit, offset).call(filter)
  }
}
