package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{FormData, HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}

import scala.concurrent.{ExecutionContext, Future}

trait LineService {
  def notify(message: String): Future[Boolean]
}

class LineServiceImpl(implicit val actor: ActorSystem, context: ExecutionContext) extends LineService {
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
      case _ => Future.successful(false)
    }
  }
}
