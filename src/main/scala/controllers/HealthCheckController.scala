package controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import models.mackerel.MackerelRequest
import services.MackerelService

import scala.concurrent.ExecutionContext

class HealthCheckController(mackerelService: MackerelService)(implicit
    system: ActorSystem[Nothing],
    context: ExecutionContext
) {
  val route: Route = {
    path("healthCheck") {
      get {
        mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
        complete(HttpEntity(ContentTypes.`application/json`, "alive"))
      }
    }
  }
}
