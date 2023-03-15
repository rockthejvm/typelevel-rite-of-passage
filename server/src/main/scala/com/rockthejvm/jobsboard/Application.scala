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
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.rockthejvm.jobsboard.modules.*
import com.rockthejvm.jobsboard.config.*
import com.rockthejvm.jobsboard.config.syntax.*
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

object Application extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    case AppConfig(
          postgresConfig,
          emberConfig,
          securityConfig,
          tokenConfig,
          emailServiceConfig,
          stripeConfig
        ) =>
      val appResource = for {
        xa      <- Database.makePostgresResource[IO](postgresConfig)
        core    <- Core[IO](xa, tokenConfig, emailServiceConfig, stripeConfig)
        httpApi <- HttpApi[IO](core, securityConfig)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(CORS(httpApi.endpoints).orNotFound) // TODO remove this when deploying
          .build
      } yield server

      appResource.use(_ => IO.println("Rock the JVM!") *> IO.never)
  }
}
