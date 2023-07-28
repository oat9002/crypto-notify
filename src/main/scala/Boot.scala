import actors.{Command, Scheduler}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import controllers.HealthCheckController
import processors.ExecuteProcessor

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import di.DependencySetup
import services.healthcheck.{MackerelService, MackerelServiceImpl}

object Boot extends App with LazyLogging with FailFastCirceSupport {
  given nothingActorRef: ActorRef[Nothing] =
    system.systemActorOf(Behaviors.empty, "crypto-notify-nothing")
  given executionContext: ExecutionContext = system.executionContext

  private val dependencies = DependencySetup()

  given system: ActorSystem[Command] =
    ActorSystem(Scheduler(), "crypto-notify")

  lazy val executor: ExecuteProcessor = dependencies.executorProcessor(system)
  lazy val healthCheckController: HealthCheckController =
    HealthCheckController()

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
