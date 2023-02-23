package com.rockthejvm.jobsboard.playground

import cats.effect.IOApp
import cats.effect.IO
import tsec.passwordhashers.jca.BCrypt
import tsec.passwordhashers.PasswordHash

object PasswordHashingPlayground extends IOApp.Simple {
  override def run: IO[Unit] =
    BCrypt.hashpw[IO]("rockthejvm").flatMap(IO.println) *>
      BCrypt.hashpw[IO]("riccardorulez").flatMap(IO.println) *>
      BCrypt.hashpw[IO]("simplepassword").flatMap(IO.println) *>
      BCrypt.hashpw[IO]("riccardorocks").flatMap(IO.println)
}
