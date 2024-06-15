package processors

import akka.actor.typed.ActorSystem
import commons.{Configuration, HttpClient}
import models.mackerel.MackerelRequest
import services.healthcheck.MackerelService

import scala.concurrent.{ExecutionContext, Future}

trait HealthCheckProcessor extends BaseProcessor

class HealthCheckProcessorImpl(using
    configuration: Configuration,
    httpClient: HttpClient,
    mackerelService: MackerelService
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends HealthCheckProcessor {

  override def run(): Future[Boolean] = {
    mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
  }
}
