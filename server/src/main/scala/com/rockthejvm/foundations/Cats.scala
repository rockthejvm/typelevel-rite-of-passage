package com.rockthejvm.foundations

object Cats {

  /*
    type classes
    - Applicative
    - Functor
    - FlatMap
    - Monad
    - ApplicativeError/MonadError
  */

  // functor - "mappable" structures
  trait MyFunctor[F[_]] {
    def map[A, B](initialValue: F[A])(f: A => B): F[B]
  }

  import cats.Functor
  import cats.instances.list.*
  val listFunctor = Functor[List]
  val mappedList = listFunctor.map(List(1,2,3))(_ + 1)

  // generalizable "mappable" APIs
  def increment[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ + 1)

  import cats.syntax.functor.*
  def increment_v2[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    container.map(_ + 1)

  // applicative - pure, wrap existing values into "wrapper" values
  trait MyApplicative[F[_]] extends Functor[F] {
    def pure[A](value: A): F[A]
  }

  import cats.Applicative
  val applicativeList = Applicative[List]
  val aSimpleList = applicativeList.pure(42)
  import cats.syntax.applicative.*
  val aSimpleList_v2 = 42.pure[List]

  // flatMap
  trait MyFlatMap[F[_]] extends Functor[F] {
    def flatMap[A, B](initialValue: F[A])(f: A => F[B]): F[B]
  }

  import cats.FlatMap
  val flatMapList = FlatMap[List]
  val flatMappedList = flatMapList.flatMap(List(1,2,3))(x => List(x, x + 1))
  import cats.syntax.flatMap.*
  def crossProduct[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    containerA.flatMap(a => containerB.map(b => (a, b)))

  def crossProduct_v2[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    for {
      a <- containerA
      b <- containerB
    } yield (a, b)

  // Monad - applicative + flatMap
  trait MyMonad[F[_]] extends Applicative[F] with FlatMap[F] {
    override def map[A, B](fa: F[A])(f: A => B) =
      flatMap(fa)(a => pure(f(a)))
  }

  import cats.Monad
  val monadList = Monad[List]
  def crossProduct_v2[F[_]: Monad, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    for {
      a <- containerA
      b <- containerB
    } yield (a, b)

  // applicative-error - computations that can fail
  trait MyApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](error: E): F[A]
  }

  import cats.ApplicativeError
  type ErrorOr[A] = Either[String, A]
  val applicativeEither = ApplicativeError[ErrorOr, String]
  val desiredValue: ErrorOr[Int] = applicativeEither.pure(42)
  val failedValue: ErrorOr[Int] = applicativeEither.raiseError("Something bad happened")
  import cats.syntax.applicativeError.*
  val failedValue_v2: ErrorOr[Int] = "Something bad happened".raiseError

  // monad-error
  trait MyMonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F]
  import cats.MonadError
  val monadErrorEither = MonadError[ErrorOr, String]

  def main(args: Array[String]): Unit = {

  }
}

