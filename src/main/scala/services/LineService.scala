package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, HttpClient}
import models.line.LineResponse

import scala.concurrent.{ExecutionContext, Future}

trait LineService {
  def notify(message: String): Future[Boolean]
}

class LineServiceImpl(httpClient: HttpClient, configuration: Configuration)(
    implicit
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends LineService
    with LazyLogging {
  override def notify(message: String): Future[Boolean] = {
    val response = httpClient.postFormData[LineResponse](
      configuration.lineConfig.url,
      Map("message" -> message),
      Map(
        "Authorization" -> s"Bearer ${configuration.lineConfig.lineNotifyToken}"
      )
    )

    response.map {
      case Left(err) =>
        logger.error(s"Line notify unexpected error, $err")
        false
      case Right(_) => true
    }
  }
}
