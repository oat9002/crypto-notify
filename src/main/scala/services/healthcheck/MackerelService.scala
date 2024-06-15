package services.healthcheck

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, Constant, HttpClient, Logger}
import io.circe.syntax.*
import models.mackerel.{MackerelRequest, MackerelResponse}
import services.healthcheck.MackerelService

import scala.concurrent.{ExecutionContext, Future}

trait MackerelService {
  def sendMeasurement(request: List[MackerelRequest]): Future[Boolean]
}

class MackerelServiceImpl(using configuration: Configuration, httpClient: HttpClient)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext,
                                                                                      logger: Logger
) extends MackerelService {
  val baseUrl: String = Constant.makerelUrl
  val serviceName: String =
    configuration.mackerelConfig.map(_.serviceName).getOrElse("")
  val apiKey: String = configuration.mackerelConfig.map(_.apiKey).getOrElse("")

  override def sendMeasurement(
      request: List[MackerelRequest]
  ): Future[Boolean] = {
    val url =
      s"$baseUrl/api/v0/services/$serviceName/tsdb"
    val response = httpClient.post[List[MackerelRequest], MackerelResponse](
      url,
      request,
      Map("X-Api-Key" -> s"$apiKey")
    )

    response.map {
      case Left(err) =>
        logger.error(
          s"Send measurement failed, Error: $err, ${request.asJson.spaces2}"
        )
        false
      case Right(res) => res.success
    }
  }
}
