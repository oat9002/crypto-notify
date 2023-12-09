import actors.{Command, Scheduler}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import controllers.HealthCheckController
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import di.DependencySetup
import processors.{ExecuteProcessor, ExecutorProcessorImpl}
import services.scheduler.{QuartzService, QuartzServiceImpl}

import scala.concurrent.ExecutionContext

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
