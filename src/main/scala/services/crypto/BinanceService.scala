package services.crypto

import org.apache.pekko.actor.typed.ActorSystem
import commons.Constant.EncryptionAlgorithm
import commons.*
import models.CryptoBalance
import models.binance.{Coin, Saving, Ticker}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

trait BinanceService {
  def getSaving: Future[Option[Saving]]
  def getAccountDetail: Future[Option[List[Coin]]]
  def getLatestPrice: Future[Option[List[Ticker]]]
  def getAllBalance: Future[Option[List[CryptoBalance]]]
}

class BinanceServiceImpl(using
    configuration: Configuration,
    httpClient: HttpClient,
    logger: Logger
)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends BinanceService {
  val recvWindow: FiniteDuration = 10.seconds
  val baseUrl: String = Constant.binanceUrl
  val secretKey: String =
    configuration.binanceConfig.map(_.secretKey).getOrElse("")
  val apiKey: String = configuration.binanceConfig.map(_.apiKey).getOrElse("")

  override def getSaving: Future[Option[Saving]] = {
    val timeStamp = System.currentTimeMillis()
    val queryString = s"timestamp=$timeStamp&recvWindow=${recvWindow.toMillis}"
    val signature = CommonUtil.generateHMAC(
      queryString,
      configuration.binanceConfig.map(_.secretKey).getOrElse(""),
      EncryptionAlgorithm.HmacSHA256
    )
    val url =
      s"$baseUrl/sapi/v1/simple-earn/flexible/position?$queryString&signature=$signature"
    val response = httpClient.get[Saving](url, Map("X-MBX-APIKEY" -> apiKey))

    response map {
      case Left(err) =>
        logger.error(s"getSaving failed: $err")
        None
      case Right(saving) => Some(saving)
    }
  }

  override def getAccountDetail: Future[Option[List[Coin]]] = {
    val timeStamp = System.currentTimeMillis()
    val queryString = s"timestamp=$timeStamp&recvWindow=${recvWindow.toMillis}"
    val signature = CommonUtil.generateHMAC(
      queryString,
      secretKey,
      EncryptionAlgorithm.HmacSHA256
    )
    val url = s"$baseUrl/sapi/v1/capital/config/getall?$queryString&signature=$signature"
    val response = httpClient.get[List[Coin]](url, Map("X-MBX-APIKEY" -> apiKey))

    response map {
      case Left(err) =>
        logger.error(s"getAccountDetail failed: $err")
        None
      case Right(saving) => Some(saving)
    }
  }

  override def getLatestPrice: Future[Option[List[Ticker]]] = {
    val url = s"$baseUrl/api/v3/ticker/price"
    val response = httpClient.get[List[Ticker]](url)

    response map {
      case Left(err) =>
        logger.error(s"getLatestPrice failed: $err")
        None
      case Right(price) => Some(price)
    }
  }

  override def getAllBalance: Future[Option[List[CryptoBalance]]] = {
    val savingF = getSaving
    val accountDetailF = getAccountDetail

    for {
      saving <- savingF
      accountDetail <- accountDetailF
    } yield {
      val fromSaving = saving
        .map(_.rows.map(x => x.asset.toLowerCase() -> x.totalAmount))
        .getOrElse(List.empty)
      val fromAccount = accountDetail
        .map(x =>
          x.filter(_.free != 0)
            .map(x => x.coin.toLowerCase() -> x.free)
        )
        .getOrElse(List.empty)

      val binanceMap = (fromSaving ++ fromAccount)
        .groupBy(_._1)
        .map { case (k, v) =>
          k -> v.map(_._2).sum
        }

      if (binanceMap.isEmpty) {
        None
      } else {
        Some(binanceMap.map(x => CryptoBalance(symbol = x._1, balance = x._2)).toList)
      }
    }
  }
}
