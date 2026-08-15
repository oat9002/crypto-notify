import actors.{Command, Scheduler}
import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.*
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import commons.{Configuration, LocalLogger}
import controllers.{HealthCheckController, NotifyController}
import di.DependencySetup
import processors.{ExecuteProcessor, ExecutorProcessorImpl, NotifyProcessor}
import services.scheduler.{QuartzService, QuartzServiceImpl}

import scala.concurrent.ExecutionContext

object Boot extends LocalLogger with FailFastCirceSupport {
  @main def run(): Unit = {
    given nothingSystem: ActorSystem[Nothing] =
      ActorSystem(Behaviors.empty, "crypto-notify-nothing")

    given executionContext: ExecutionContext = nothingSystem.executionContext

    val diSetup = DependencySetup()

    given system: ActorSystem[Command] =
      ActorSystem(Scheduler(diSetup.notifyProcessor, diSetup.healthCheckProcessor), "crypto-notify")

    given quartService: QuartzService[Command] = QuartzServiceImpl[Command]()

    given configuration: Configuration = diSetup.configuration

    given notifyProcessor: NotifyProcessor = diSetup.notifyProcessor

    val executor: ExecuteProcessor = ExecutorProcessorImpl()
    val healthCheckController: HealthCheckController = HealthCheckController()
    val notifyController: NotifyController = controllers.NotifyController()
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
        healthCheckController.route,
        notifyController.route
      )

    if (configuration.appConfig.useScheduler) {
      executor.run()
    }

    Http().newServerAt("0.0.0.0", configuration.appConfig.port).bind(route)

    logger.info(
      s"Server online at http://localhost:${configuration.appConfig.port}/"
    )
  }
}
