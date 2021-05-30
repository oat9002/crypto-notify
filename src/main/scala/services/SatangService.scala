package services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import commons.{CommonUtil, Configuration}
import models.{Ticker, User}

import scala.concurrent.{ExecutionContext, Future}

trait SatangService {
  def getUser(userId: String): Future[Option[User]]
  def getCryptoPrice(pair: String): Future[Option[Ticker]]
  def getCryptoPrices: Future[Option[Array[Ticker]]]
}

class SatangServiceImpl(configuration: Configuration)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends SatangService {
  import commons.HttpResponseUtil._
  import commons.JsonUtil._

  val url: String = configuration.satangConfig.url

  override def getUser(userId: String): Future[Option[User]] = {
    val userUrl: String = url + "users/"
    val signature = CommonUtil.generateHMAC512("", configuration.satangConfig.apiSecret)
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
      case Some(x) => Some(x.toObject(classOf[User]))
      case _ => None
    }
  }

  override def getCryptoPrice(pair: String): Future[Option[Ticker]] = {
    val tickerUrl = url + s"v3/ticker/24hr?symbol=$pair"
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = tickerUrl,
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toJsonString
      case _ => Future.successful(None)
    }.map {
      case Some(x) => Some(x.toObject(classOf[Ticker]))
      case _ => None
    }
  }

  override def getCryptoPrices: Future[Option[Array[Ticker]]] = {
    val tickerUrl = url + "v3/ticker/24hr"
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = tickerUrl,
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toJsonString
      case _ => Future.successful(None)
    }.map {
      case Some(x) => Some(x.toObject(classOf[Array[Ticker]]))
      case _ => None
    }
  }
}
