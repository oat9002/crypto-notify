package controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import models.mackerel.*
import services.healthcheck.MackerelService

import scala.concurrent.ExecutionContext

class HealthCheckController(mackerelService: MackerelService)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends FailFastCirceSupport {
  val route: Route = {
    path("healthCheck") {
      get {
        mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
        complete(HttpEntity(ContentTypes.`application/json`, "alive"))
      }
    }
  }
}
