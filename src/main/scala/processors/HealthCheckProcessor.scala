package processors

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import models.mackerel.MackerelRequest
import services.healthcheck.{MackerelService, MackerelServiceImpl}

import scala.concurrent.{ExecutionContext, Future}

trait HealthCheckProcessor extends BaseProcessor

class HealthCheckProcessorImpl(using
    configuration: Configuration,
    httpClient: HttpClient,
    mackerelService: MackerelService
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends HealthCheckProcessor
    with LazyLogging {

  override def run(): Future[Boolean] = {
    mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
  }
}
