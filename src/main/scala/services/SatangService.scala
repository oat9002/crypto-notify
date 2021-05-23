package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl, EncryptionUtil, EncryptionUtilImpl}
import models.GetBalanceResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

trait SatangService {
  def getBalance(userId: String): Future[Option[GetBalanceResponse]]
}

class SatangServiceImpl(implicit actor: ActorSystem, context: ExecutionContext) extends SatangService {
  import commons.JsonUtil._
  import commons.HttpResponseUtil._

  lazy val configuration: Configuration = wire[ConfigurationImpl]
  lazy val encriptionUtil: EncryptionUtil = wire[EncryptionUtilImpl]
  val url: String = configuration.satangConfig.url
  val userUrl: String = url + "users/"

  def getBalance(userId: String): Future[Option[GetBalanceResponse]] = {
    val signature = encriptionUtil.generateHMAC512("", configuration.satangConfig.apiSecret)
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = userUrl + s"/$userId",
      headers = Seq[HttpHeader](RawHeader("Authorization", s"TDAX-API ${configuration.satangConfig.apiKey}"),
        RawHeader("Signature", s"$signature"))
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toJsonString
      case _ => Future.successful(None)
    }.map {
      case Some(x) => Some(x.toObject(classOf[GetBalanceResponse]))
      case _ => None
    }
  }
}
