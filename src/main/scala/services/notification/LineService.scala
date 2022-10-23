package services.notification

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, ConfigurationImpl, Constant, HttpClient}
import models.line.LineResponse
import services.notification.LineService

import scala.concurrent.{ExecutionContext, Future}

trait LineService extends NotificationService

class LineServiceImpl(httpClient: HttpClient, configuration: Configuration)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends LineService
    with LazyLogging {
  override def notify(message: String): Future[Boolean] = {
    val response = httpClient.postFormData[LineResponse](
      Constant.lineNotifyUrl,
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
