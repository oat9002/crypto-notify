package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{CommonUtil, Configuration, HmacAlgorithm, HttpClient}
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

class BinanceServiceImpl(configuration: Configuration, httpClient: HttpClient)(
    using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends BinanceService
    with LazyLogging {
  val recvWindow: FiniteDuration = 10.seconds
  val baseUrl: String = configuration.binanceConfig.map(_.url).getOrElse("")
  val secretKey: String =
    configuration.binanceConfig.map(_.secretKey).getOrElse("")
  val apiKey: String = configuration.binanceConfig.map(_.apiKey).getOrElse("")

  override def getSaving: Future[Option[Saving]] = {
    val timeStamp = System.currentTimeMillis()
    val queryString = s"timestamp=$timeStamp&recvWindow=${recvWindow.toMillis}"
    val signature = CommonUtil.generateHMAC(
      queryString,
      configuration.binanceConfig.map(_.secretKey).getOrElse(""),
      HmacAlgorithm.HmacSHA256
    )
    val url =
      s"$baseUrl/sapi/v1/lending/union/account?$queryString&signature=$signature"
    val response = httpClient.get[Saving](
      url,
      Map("X-MBX-APIKEY" -> apiKey)
    )

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
      HmacAlgorithm.HmacSHA256
    )
    val url =
      s"$baseUrl/sapi/v1/capital/config/getall?$queryString&signature=$signature"
    val response = httpClient.get[Array[Coin]](
      url,
      Map("X-MBX-APIKEY" -> apiKey)
    )

    response map {
      case Left(err) =>
        logger.error(s"getAccountDetail failed: $err")
        None
      case Right(saving) => Some(saving.toList)
    }
  }

  override def getLatestPrice: Future[Option[List[Ticker]]] = {
    val url = s"$baseUrl/api/v3/ticker/price"
    val response = httpClient.get[Array[Ticker]](url)

    response map {
      case Left(err) =>
        logger.error(s"getLatestPrice failed: $err")
        None
      case Right(price) => Some(price.toList)
    }
  }

  override def getAllBalance: Future[Option[List[CryptoBalance]]] = {
    for {
      savingOpt <- getSaving
      accountDetailOpt <- getAccountDetail
    } yield for {
      saving <- savingOpt
      accountDetail <- accountDetailOpt
    } yield {
      val binanceMap = (saving.positionAmountVos.map(x =>
        x.asset.toLowerCase() -> x.amount
      ) ++ accountDetail
        .filter(_.free != 0)
        .map(x => x.coin.toLowerCase() -> x.free)).groupBy(_._1).map {
        case (k, v) => k -> v.map(_._2).sum
      }

      binanceMap.map(x => CryptoBalance(symbol = x._1, balance = x._2)).toList
    }
  }
}
