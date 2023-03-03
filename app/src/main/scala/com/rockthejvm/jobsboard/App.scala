package com.rockthejvm.jobsboard

import scala.scalajs.js.annotation.*
import org.scalajs.dom.document

@JSExportTopLevel("RockTheJvmApp")
class App {
  @JSExport
  def doSomething(containerId: String) =
    document.getElementById(containerId).innerHTML = "Scala Rocks the JVM!"
}
