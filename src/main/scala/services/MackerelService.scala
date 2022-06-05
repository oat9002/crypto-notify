package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.JsonUtil.JsonSerialize
import commons.{Configuration, HttpClient}
import models.mackerel.MackerelRequest

import scala.concurrent.{ExecutionContext, Future}

trait MackerelService {
  def sendMeasurement(request: List[MackerelRequest]): Future[Boolean]
}

class MackerelServiceImpl(configuration: Configuration, httpClient: HttpClient)(
    implicit
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends MackerelService
    with LazyLogging {
  override def sendMeasurement(
      request: List[MackerelRequest]
  ): Future[Boolean] = {
    val url =
      s"${configuration.mackerelConfig.url}/api/v0/services/${configuration.mackerelConfig.serviceName}/tsdb"
    val response = httpClient.post[List[MackerelRequest], Any](
      url,
      request,
      Map("X-Api-Key" -> s"${configuration.mackerelConfig.apiKey}")
    )

    response.map {
      case Left(err) =>
        logger.error(s"Send measurement failed, Error: $err, ${request.toJson}")
        false
      case _ => true
    }
  }
}
