package com.rockthejvm.foundations

import cats.effect.{IO, IOApp, MonadCancelThrow}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import doobie.implicits.*
import doobie.util.ExecutionContexts

object Doobie extends IOApp.Simple {

  case class Student(id: Int, name: String)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO] (
    "org.postgresql.Driver", // JDBC connector
    "jdbc:postgresql:demo", // database URL
    "docker", // user
    "docker" // pass
  )

  // read
  def findAllStudentNames: IO[List[String]] = {
    val query = sql"select name from students".query[String]
    val action = query.to[List]
    action.transact(xa)
  }

  // write
  def saveStudent(id: Int, name: String): IO[Int] = {
    val query = sql"insert into students(id, name) values ($id, $name)"
    val action = query.update.run
    action.transact(xa)
  }

  // read as CC with fragments
  def findStudentsByInitial(letter: String): IO[List[Student]] = {
    val selectPart = fr"select id, name"
    val fromPart = fr"from students"
    val wherePart = fr"where left(name, 1) = $letter"

    val statement = selectPart ++ fromPart ++ wherePart
    val action = statement.query[Student].to[List]
    action.transact(xa)
  }

  // organize code
  trait Students[F[_]] { // "repository"
    def findById(id: Int): F[Option[Student]]
    def findAll: F[List[Student]]
    def create(name: String): F[Int]
  }

  object Students {
    def make[F[_]: MonadCancelThrow](xa: Transactor[F]): Students[F] = new Students[F] {
      def findById(id: Int): F[Option[Student]] =
        sql"select id, name from students where id=$id".query[Student].option.transact(xa)

      def findAll: F[List[Student]] =
        sql"select id, name from students".query[Student].to[List].transact(xa)

      def create(name: String): F[Int] =
        sql"insert into students(name) values ($name)".update.withUniqueGeneratedKeys[Int]("id").transact(xa)
    }
  }

  val postgresResource = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](16)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver", // JDBC connector
      "jdbc:postgresql:demo", // database URL
      "docker", // user
      "docker", // pass
      ec
    )
  } yield xa

  val smallProgram = postgresResource.use { xa =>
    val studentsRepo = Students.make[IO](xa)
    for {
      id <- studentsRepo.create("Daniel")
      daniel <- studentsRepo.findById(id)
      _ <- IO.println(s"The first student of Rock the JVM is $daniel")
    } yield ()
  }

  override def run = smallProgram
}
