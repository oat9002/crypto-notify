import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.softwaremill.macwire.wire
import services.{JobRunrService, JobRunrServiceImpl}

import scala.concurrent.ExecutionContextExecutor

object Boot extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  lazy val jobRunrService: JobRunrService = wire[JobRunrServiceImpl]

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
