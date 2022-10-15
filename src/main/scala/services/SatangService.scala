package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{CommonUtil, Configuration, HttpClient}
import models.satang.{Ticker, User}
import commons.Constant.EncryptionAlgorithm
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

trait SatangService {
  def getUser(userId: String): Future[Option[User]]
  def getCryptoPrice(pair: String): Future[Option[Ticker]]
  def getCryptoPrices: Future[Option[List[Ticker]]]
}

class SatangServiceImpl(configuration: Configuration, httpClient: HttpClient)(
    implicit
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends SatangService
    with LazyLogging {

  val url: String = configuration.satangConfig.url

  override def getUser(userId: String): Future[Option[User]] = {
    val userUrl: String = url + "users/"
    val signature =
      CommonUtil.generateHMAC(
        "",
        configuration.satangConfig.apiSecret,
        EncryptionAlgorithm.HmacSHA512
      )
    val response = httpClient.get[User](
      userUrl + s"/$userId",
      Map(
        "Authorization" -> s"TDAX-API ${configuration.satangConfig.apiKey}",
        "Signature" -> s"$signature"
      )
    )

    response.map {
      case Left(err) =>
        logger.error(s"getUser unexpected error, $err")
        None
      case Right(x) => Some(x)
    }
  }

  override def getCryptoPrice(pair: String): Future[Option[Ticker]] = {
    val tickerUrl = url + s"v3/ticker/24hr?symbol=$pair"
    val response = httpClient.get[Ticker](tickerUrl)

    response.map {
      case Left(err) =>
        logger.error(s"getCryptoPrice unexpected error, $err")
        None
      case Right(x) => Some(x)
    }
  }

  override def getCryptoPrices: Future[Option[List[Ticker]]] = {
    val tickerUrl = url + "v3/ticker/24hr"
    val response = httpClient.get[Array[Ticker]](tickerUrl)

    response.map {
      case Left(err) =>
        logger.error(s"getCryptoPrices unexpected error, $err")
        None
      case Right(x) => Some(x.toList)
    }
  }
}
