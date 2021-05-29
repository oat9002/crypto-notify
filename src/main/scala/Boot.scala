import actors.NotifyJob
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.softwaremill.macwire.wire
import processors.{Executor, ExecutorImpl}
import services.JobRunrService

import scala.concurrent.ExecutionContextExecutor

object Boot extends App {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "crypto-notify")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  JobRunrService.initialize
  lazy val executor = wire[ExecutorImpl]

  val route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, "Say hello to crypto-notify"))
      }
    }

  executor.execute()
  Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server online at http://localhost:8080/")
}
