import actors.{Command, Scheduler}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import controllers.HealthCheckController
import processors.{Executor, ExecutorImpl}
import services.{MackerelService, MackerelServiceImpl}

import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.server.Route

object Boot extends App with LazyLogging {
  given system: ActorSystem[Command] =
    ActorSystem(Scheduler(), "crypto-notify")
  given nothingActorRef: ActorRef[Nothing] =
    system.systemActorOf(Behaviors.empty, "crypto-notify-nothing")
  given executionContext: ExecutionContextExecutor =
    system.executionContext
  lazy val httpClient: HttpClient = HttpClientImpl()
  lazy val configuration: Configuration = ConfigurationImpl()
  lazy val mackerelService: MackerelService =
    MackerelServiceImpl(configuration, httpClient)
  lazy val executor: Executor = ExecutorImpl(configuration)
  lazy val healthCheckController: HealthCheckController =
    HealthCheckController(mackerelService)

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
