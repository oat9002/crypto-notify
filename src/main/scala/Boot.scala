import actors.{Command, Scheduler}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import controllers.HealthCheckController
import processors.{Executor, ExecutorImpl}
import services.{MackerelService, MackerelServiceImpl}

import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.server.Route

object Boot extends App with LazyLogging {
  implicit val system: ActorSystem[Command] =
    ActorSystem(Scheduler(), "crypto-notify")
  implicit val nothingActorRef: ActorRef[Nothing] =
    system.systemActorOf(Behaviors.empty, "crypto-notify-nothing")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext
  lazy val httpClient: HttpClient = wire[HttpClientImpl]
  lazy val configuration: Configuration = wire[ConfigurationImpl]
  lazy val mackerelService: MackerelService = wire[MackerelServiceImpl]
  lazy val executor: Executor = wire[ExecutorImpl]
  lazy val healthCheckController: HealthCheckController =
    wire[HealthCheckController]

  val route: Route =
    concat(
      pathEndOrSingleSlash {
        get {
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              "Say hello to crypto-notify"
            )
          )
        }
      },
      healthCheckController.route
    )

  executor.execute()
  Http().newServerAt("0.0.0.0", configuration.appConfig.port).bind(route)

  logger.info(
    s"Server online at http://localhost:${configuration.appConfig.port}/"
  )
}
