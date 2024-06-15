package services.notification

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.*
import models.line.LineResponse

import scala.concurrent.{ExecutionContext, Future}

trait LineService {
  def notify(message: String): Future[Boolean]
}

class LineServiceImpl(using httpClient: HttpClient, configuration: Configuration)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends LineService
    with LocalLogger {
  override def notify(message: String): Future[Boolean] = {
    val response = httpClient.postFormData[LineResponse](
      Constant.lineNotifyUrl,
      Map("message" -> message),
      Map(
        "Authorization" -> s"Bearer ${configuration.lineConfig.map(_.lineNotifyToken).getOrElse("")}"
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
