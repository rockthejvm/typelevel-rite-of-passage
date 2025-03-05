# Typelevel Rite of Passage - Scala jobs board

This is the code that we write in the [Typelevel Rite of Passage](https://rockthejvm.com/courses/typelevel-rite-of-passage) course on Rock the JVM.

This repository contains the application we write in the course: a jobs board for Scala developers. The application features
- user management with role-based access control
- an email system
- user-generated content with Markdown support
- full credit card checkout with Stripe checkout sessions
- various forms of data storage and retrieval (CRUD-style)

You can find a live version of the app [here](https://companies.rockthejvm.com).

The application is built with 
- [Cats Effect](https://typelevel.org/cats-effect) for effects
- [Cats](https://typelevel.org/cats) for functional programming
- Doobie for data storage
- http4s for HTTP server
- other Typelevel libraries for various purposes: Circe, PureConfig, FS2, log4cats, etc
- Flyway for migrations
- Java mail for emails
- Stripe for checkout
- [Tyrian](https://tyrian.indigoengine.io/) on the frontend

## How to run

-   have a docker database ready - this means, for dev purposes, `docker-compose up` in the root folder
-   in another terminal `sbt`, then `project server`, then `~compile` to compile the server incrementally as you develop
-   in the same terminal `runMain com.rockthejvm.jobsboard.Application` to start the server 
-   in another terminal `sbt`, `project app` and `~fastOptJS` to compile the frontend
-   in another terminal (that's 4 in total), go to the `app/` directory, run `npm install`
-   still in terminal 4, run `npm run start` to start serving the page
-   go to `http://localhost:1234` to see the page

## For questions or suggestions

If you have changes to suggest to this repo, either
- submit a GitHub issue
- tell me in the course Q/A forum
- submit a pull request!