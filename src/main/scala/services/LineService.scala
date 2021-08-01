package services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{FormData, HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.HttpResponseUtil.ToJsonString
import commons.{Configuration, ConfigurationImpl}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait LineService {
  def notify(message: String): Future[Boolean]
}

class LineServiceImpl(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends LineService with LazyLogging {
  lazy val configuration: Configuration = wire[ConfigurationImpl]

  override def notify(message: String): Future[Boolean] = {
    val response = Http().singleRequest(HttpRequest(
      uri = configuration.lineConfig.url,
      method = HttpMethods.POST,
      entity = FormData(Map("message" -> message)).toEntity,
      headers = Seq[HttpHeader](RawHeader("Authorization", s"Bearer ${configuration.lineConfig.lineNotifyToken}"))
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.discardBytes().future().map(_ => true)
      case HttpResponse(_, _, entity, _) =>
        entity.toJsonString.onComplete {
          case Success(Some(v)) => logger.error(s"line notify: $v")
          case _ => logger.error("Line notify unexpected error")
        }
        Future.successful(false)
      case _ => Future.successful(false)
    }
  }
}
