package com.rockthejvm.jobsboard.components

import tyrian.*
import tyrian.http.*
import tyrian.Html.*
import cats.effect.IO
import io.circe.generic.auto.*

import com.rockthejvm.jobsboard.*
import com.rockthejvm.jobsboard.common.*
import com.rockthejvm.jobsboard.domain.job.*
import org.scalajs.dom.HTMLInputElement
import tyrian.cmds.Logger

final case class FilterPanel(
    possibleFilters: JobFilter = JobFilter(),
    selectedFilters: Map[String, Set[String]] = Map(),
    maybeError: Option[String] = None,
    maxSalary: Int = 0,
    remote: Boolean = false,
    dirty: Boolean = false,
    filterAction: Map[String, Set[String]] => App.Msg = _ => App.NoOp
) extends Component[App.Msg, FilterPanel] {
  import FilterPanel.*

  override def initCmd: Cmd[IO, App.Msg] =
    Commands.getFilters

  override def update(msg: App.Msg): (FilterPanel, Cmd[IO, App.Msg]) = msg match {
    case TriggerFilter =>
      (this.copy(dirty = false), Cmd.Emit(filterAction(selectedFilters)))
    case SetPossibleFilters(possibleFilters) =>
      (this.copy(possibleFilters = possibleFilters), Cmd.None)
    case FilterPanelError(e) =>
      (this.copy(maybeError = Some(e)), Cmd.None)
    // content update
    case UpdateSalaryInput(salary) =>
      (this.copy(maxSalary = salary, dirty = true), Cmd.None)
    case UpdateRemote(remote) =>
      (this.copy(remote = remote, dirty = true), Cmd.None)
    case UpdateValueChecked(groupName, value, checked) =>
      val oldGroup  = selectedFilters.get(groupName).getOrElse(Set())
      val newGroup  = if (checked) oldGroup + value else oldGroup - value
      val newGroups = selectedFilters + (groupName -> newGroup)
      (
        this.copy(selectedFilters = newGroups, dirty = true),
        Logger.consoleLog[IO](s"Filters: $newGroups")
      )
    case _ => (this, Cmd.None)
  }

  override def view(): Html[App.Msg] =
    div(`class` := "filter-panel-container")(
      maybeRenderError(),
      renderSalaryFilter(),
      renderRemoteCheckbox(),
      renderCheckboxGroup("Companies", possibleFilters.companies),
      renderCheckboxGroup("Locations", possibleFilters.locations),
      renderCheckboxGroup("Countries", possibleFilters.countries),
      renderCheckboxGroup("Tags", possibleFilters.tags),
      renderCheckboxGroup("Seniorities", possibleFilters.seniorities),
      renderApplyFiltersButton()
    )

  // private

  private def maybeRenderError() =
    maybeError
      .map { e =>
        div(`class` := "filter-panel-error")(e)
      }
      .getOrElse(div())

  private def renderSalaryFilter() =
    div(`class` := "filter-group")(
      h6(`class` := "filter-group-header")("Salary"),
      div(`class` := "filter-group-content")(
        label(`for` := "filter-salary")("Min (in local currency)"),
        input(
          `type` := "number",
          id     := "filter-salary",
          onInput(s => UpdateSalaryInput(if (s.isEmpty) 0 else s.toInt))
        )
      )
    )

  private def renderRemoteCheckbox() =
    div(`class` := "filter-group-content")(
      label(`for` := s"filter-checkbox")("Remote"),
      input(
        `type` := "checkbox",
        id     := s"filter-checkbox",
        checked(remote),
        onEvent(
          "change",
          event => {
            val checkbox = event.target.asInstanceOf[HTMLInputElement]
            UpdateRemote(checkbox.checked)
          }
        )
      )
    )

  private def renderCheckboxGroup(groupName: String, possibleValues: List[String]) = {
    val selectedValues = selectedFilters.get(groupName).getOrElse(Set())
    div(`class` := "filter-group")(
      h6(`class` := "filter-group-header")(groupName),
      div(`class` := "filter-group-content")(
        possibleValues.map(value => renderCheckbox(groupName, value, selectedValues))
      )
    )
  }

  private def renderCheckbox(groupName: String, value: String, selectedValues: Set[String]) =
    div(`class` := "filter-group-content")(
      label(`for` := s"filter-$groupName-$value")(value),
      input(
        `type` := "checkbox",
        id     := s"filter-$groupName-$value",
        checked(selectedValues.contains(value)),
        onEvent(
          "change",
          event => {
            // send a message to insert value as a checked value inside the groupName's Set int the map
            val checkbox = event.target.asInstanceOf[HTMLInputElement]
            UpdateValueChecked(groupName, value, checkbox.checked)
          }
        )
      )
    )

  private def renderApplyFiltersButton() =
    button(
      `type` := "button",
      disabled(!dirty),
      onClick(TriggerFilter)
    )("Apply Filters")
}

object FilterPanel {
  trait Msg                                                 extends App.Msg
  case object TriggerFilter                                 extends Msg
  case class FilterPanelError(error: String)                extends Msg
  case class SetPossibleFilters(possibleFilters: JobFilter) extends Msg
  // content
  case class UpdateSalaryInput(salary: Int)                                         extends Msg
  case class UpdateValueChecked(groupName: String, value: String, checked: Boolean) extends Msg
  case class UpdateRemote(remote: Boolean)                                          extends Msg

  object Endpoints {
    val getFilters = new Endpoint[Msg] {
      override val location: String          = Constants.endpoints.filters
      override val method: Method            = Method.Get
      override val onError: HttpError => Msg = e => FilterPanelError(e.toString)
      override val onResponse: Response => Msg =
        Endpoint.onResponse[JobFilter, Msg](
          SetPossibleFilters(_),
          FilterPanelError(_)
        )
    }
  }

  object Commands {
    def getFilters = Endpoints.getFilters.call()
  }
}
