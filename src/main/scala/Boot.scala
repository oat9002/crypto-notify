import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl}
import processors.{Executor, ExecutorImpl}

import scala.concurrent.ExecutionContextExecutor

object Boot extends App with LazyLogging {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "crypto-notify")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  lazy val configuration: Configuration = wire[ConfigurationImpl]
  lazy val executor: Executor = wire[ExecutorImpl]

  val route =
    concat(
      pathEndOrSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, "Say hello to crypto-notify"))
        }
      },
      path("healthCheck") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, "alive"))
        }
      }
    )

  executor.execute()
  Http().newServerAt("0.0.0.0", configuration.appConfig.port).bind(route)

  logger.info(s"Server online at http://localhost:${configuration.appConfig.port}/")
}
