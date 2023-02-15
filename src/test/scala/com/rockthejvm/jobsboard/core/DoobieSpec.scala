package com.rockthejvm.jobsboard.core

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import org.testcontainers.containers.PostgreSQLContainer
import doobie.hikari.HikariTransactor

trait DoobieSpec {

  // to be implemented by whatever test case interacts with the DB
  val initScript: String

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val container: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScript)
      container.start()
      container
    }
    val release = (container: PostgreSQLContainer[Nothing]) => IO(container.stop())
    Resource.make(acquire)(release)
  }

  val transactor: Resource[IO, Transactor[IO]] = for {
    db <- postgres
    ce <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      db.getJdbcUrl(),
      db.getUsername(),
      db.getPassword(),
      ce
    )
  } yield xa
}
