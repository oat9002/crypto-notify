package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl, JsonUtil, JsonUtilImpl}
import models.GetBalanceResponse

import scala.concurrent.duration.{DurationInt}
import scala.concurrent.{ExecutionContext, Future}

trait SatangService {
  def getBalance(userId: String): Future[Option[GetBalanceResponse]]
}

class SatangServiceImpl(implicit val actor: ActorSystem, context: ExecutionContext) extends SatangService {
  lazy val configuration: Configuration = wire[ConfigurationImpl]
  lazy val jsonUtil: JsonUtil = wire[JsonUtilImpl]
  val url: String = configuration.satangConfig.url + "users/"

  def getBalance(userId: String): Future[Option[GetBalanceResponse]] = {
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = url + s"/$userId"
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toStrict(5.seconds).map(e => e.getData()).map(data => Some(data.utf8String))
      case _ => Future.successful(None)
    }.map {
      case Some(x) => Some(jsonUtil.deserialize[GetBalanceResponse](x))
      case _ => None
    }
  }
}
