import actors.{Command, Scheduler}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import controllers.HealthCheckController
import processors.{ExecuteProcessor, ExecutorProcessorImpl, HealthCheckProcessor, HealthCheckProcessorImpl, NotifyProcessor, NotifyProcessorImpl}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import di.DependencySetup
import services.healthcheck.{MackerelService, MackerelServiceImpl}
import services.notification.NotificationService
import services.scheduler.{QuartzService, QuartzServiceImpl}
import services.user.UserService

object Boot extends App with LazyLogging with FailFastCirceSupport {
  given nothingSystem: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "crypto-notify-nothing")
  given executionContext: ExecutionContext = nothingSystem.executionContext
  val diSetup = DependencySetup()
  given system: ActorSystem[Command] =
    ActorSystem(Scheduler(diSetup.notifyProcessor, diSetup.healthCheckProcessor), "crypto-notify")
  given quartService: QuartzService[Command] = QuartzServiceImpl[Command]()
  given configuration: Configuration = diSetup.configuration
  val executor: ExecuteProcessor = ExecutorProcessorImpl()
  val healthCheckController: HealthCheckController = HealthCheckController()

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

  executor.run()
  Http().newServerAt("0.0.0.0", configuration.appConfig.port).bind(route)

  logger.info(
    s"Server online at http://localhost:${configuration.appConfig.port}/"
  )
}
