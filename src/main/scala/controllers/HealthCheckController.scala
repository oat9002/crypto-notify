package controllers

import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.apache.pekko.http.scaladsl.server.Directives.{complete, get, path}
import org.apache.pekko.http.scaladsl.server.Route
import models.mackerel.*
import services.healthcheck.MackerelService

import scala.concurrent.ExecutionContext

class HealthCheckController()(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends FailFastCirceSupport {
  val route: Route = {
    path("healthCheck") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, "alive"))
      }
    }
  }
}
