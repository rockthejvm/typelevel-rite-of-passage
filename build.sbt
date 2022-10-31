ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.0"

val catsEffectVersion          = "3.3.14"
val http4sVersion              = "0.23.15"
val circeVersion               = "0.14.0"
val doobieVersion              = "1.0.0-RC1"
val pureConfigVersion          = "0.17.1"
val log4catsVersion            = "2.4.0"
val tsecVersion                = "0.4.0"
val scalaTestVersion           = "3.2.12"
val scalaTestCatsEffectVersion = "1.4.0"
val testContainerVersion       = "1.17.3"
val logbackVersion             = "1.4.0"
val slf4jVersion               = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "typelevel-project",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"                   % catsEffectVersion,
      "org.http4s"            %% "http4s-dsl"                    % http4sVersion,
      "org.http4s"            %% "http4s-ember-server"           % http4sVersion,
      "org.http4s"            %% "http4s-circe"                  % http4sVersion,
      "io.circe"              %% "circe-generic"                 % circeVersion,
      "io.circe"              %% "circe-fs2"                     % circeVersion,
      "org.tpolecat"          %% "doobie-core"                   % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"                 % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"               % doobieVersion,
      "com.github.pureconfig" %% "pureconfig-core"               % pureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"                % log4catsVersion,
      "org.slf4j"              % "slf4j-simple"                  % slf4jVersion,
      "io.github.jmcardon"    %% "tsec-http4s"                   % tsecVersion,
      "org.typelevel"         %% "log4cats-noop"                 % log4catsVersion            % Test,
      "org.scalatest"         %% "scalatest"                     % scalaTestVersion           % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
      "org.testcontainers"     % "testcontainers"                % testContainerVersion       % Test,
      "org.testcontainers"     % "postgresql"                    % testContainerVersion       % Test,
      "ch.qos.logback"         % "logback-classic"               % logbackVersion             % Test
    )
  )
