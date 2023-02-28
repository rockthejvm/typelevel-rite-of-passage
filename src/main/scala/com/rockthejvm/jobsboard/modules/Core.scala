package com.rockthejvm.jobsboard.modules

import cats.effect.*
import cats.implicits.*
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger

import com.rockthejvm.jobsboard.core.*
import com.rockthejvm.jobsboard.config.*

final class Core[F[_]] private (val jobs: Jobs[F], val auth: Auth[F])

object Core {
  def apply[F[_]: Async: Logger](
      xa: Transactor[F]
  )(securityConfig: SecurityConfig): Resource[F, Core[F]] = {
    val coreF = for {
      jobs  <- LiveJobs[F](xa)
      users <- LiveUsers[F](xa)
      auth  <- LiveAuth[F](users)(securityConfig)
    } yield new Core(jobs, auth)

    Resource.eval(coreF)
  }
}
