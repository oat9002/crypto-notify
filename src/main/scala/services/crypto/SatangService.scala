package services.crypto

import org.apache.pekko.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.Constant.EncryptionAlgorithm
import commons.*
import models.satang.{Ticker, User}
import services.crypto.SatangService

import scala.concurrent.{ExecutionContext, Future}

trait SatangService {
  def getUser(userId: String): Future[Option[User]]
  def getCryptoPrice(pair: String): Future[Option[Ticker]]
  def getCryptoPrices: Future[Option[List[Ticker]]]
}

class SatangServiceImpl(using configuration: Configuration, httpClient: HttpClient)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext,
    logger: Logger
) extends SatangService {

  val baseUrl: String = Constant.satangUrl

  override def getUser(userId: String): Future[Option[User]] = {
    val userUrl: String = s"$baseUrl/users/me"
    val signature =
      CommonUtil.generateHMAC(
        "",
        configuration.satangConfig.apiSecret,
        EncryptionAlgorithm.HmacSHA512
      )
    val response = httpClient.get[User](
      userUrl,
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
    val tickerUrl = s"$baseUrl/v3/ticker/24hr?symbol=$pair"
    val response = httpClient.get[Ticker](tickerUrl)

    response.map {
      case Left(err) =>
        logger.error(s"getCryptoPrice unexpected error, $err")
        None
      case Right(x) => Some(x)
    }
  }

  override def getCryptoPrices: Future[Option[List[Ticker]]] = {
    val tickerUrl = s"$baseUrl/v3/ticker/24hr"
    val response = httpClient.get[Array[Ticker]](tickerUrl)

    response.map {
      case Left(err) =>
        logger.error(s"getCryptoPrices unexpected error, $err")
        None
      case Right(x) => Some(x.toList)
    }
  }
}
