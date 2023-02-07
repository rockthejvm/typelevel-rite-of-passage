package com.rockthejvm.jobsboard

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import cats.effect.*
import cats.effect.IOApp
import cats.*
import cats.implicits.*
import cats.effect.IO
import org.http4s.ember.server.EmberServerBuilder

import com.rockthejvm.jobsboard.http.routes.HealthRoutes
import com.rockthejvm.jobsboard.config.*
import com.rockthejvm.jobsboard.config.syntax.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

/*
  1 - add a plain health endpoint to our app
  2 - add minimal configuration
  3 - basic http server layout
 */
object Application extends IOApp.Simple {

  val configSource = ConfigSource.default.load[EmberConfig]

  override def run = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(HealthRoutes[IO].routes.orNotFound)
      .build
      .use(_ => IO.println("Rock the JVM!") *> IO.never)
  }
}
