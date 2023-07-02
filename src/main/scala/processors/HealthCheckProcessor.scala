package processors

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import models.mackerel.MackerelRequest
import services.healthcheck.{MackerelService, MackerelServiceImpl}

import scala.concurrent.{ExecutionContext, Future}

trait HealthCheckProcessor extends BaseProcessor

class HealthCheckProcessorImpl(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends HealthCheckProcessor
    with LazyLogging {

  private lazy val configuration: Configuration = ConfigurationImpl()
  private lazy val httpclient: HttpClient = HttpClientImpl()
  private lazy val mackerelService: MackerelService = MackerelServiceImpl(configuration, httpclient)

  override def run(): Future[Boolean] = {
    mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
  }
}
