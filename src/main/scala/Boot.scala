import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import services.{JobRunrService, JobRunrServiceImpl, SatangService, SatangServiceImpl, UserService, UserServiceImpl}

import scala.concurrent.ExecutionContextExecutor

object Boot extends App {
  implicit val system: ActorSystem = ActorSystem()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  lazy val jobRunrService: JobRunrService = wire[JobRunrServiceImpl]
  lazy val satangService: SatangService = wire[SatangServiceImpl]
  lazy val configuration: Configuration = wire[ConfigurationImpl]
  lazy val userService: UserService = wire[UserServiceImpl]

  val res = userService.getBalanceMessageForLine(configuration.satangConfig.userId)

  val route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, "Say hello to crypto-notify"))
      }
    }

  jobRunrService.initialize()
  Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/")
}
