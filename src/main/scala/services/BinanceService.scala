package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{CommonUtil, Configuration, HmacAlgorithm, HttpClient}
import models.binance.{Coin, Saving}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

trait BinanceService {
  def getSaving: Future[Option[Saving]]
  def getAccountDetail: Future[Option[List[Coin]]]
}

class BinanceServiceImpl(configuration: Configuration, httpClient: HttpClient)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends BinanceService with LazyLogging {
  val recvWindow: FiniteDuration = 10.seconds

  override def getSaving: Future[Option[Saving]]  = {
    val timeStamp = System.currentTimeMillis()
    val queryString = s"timestamp=$timeStamp&recvWindow=${recvWindow.toMillis}"
    val signature = CommonUtil.generateHMAC(queryString, configuration.binanceConfig.secretKey, HmacAlgorithm.HmacSHA256)
    val url = s"${configuration.binanceConfig.url}/sapi/v1/lending/union/account?$queryString&signature=$signature"
    val response = httpClient.get[Any, Saving](url, None, Map("X-MBX-APIKEY" -> configuration.binanceConfig.apiKey))

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
    val signature = CommonUtil.generateHMAC(queryString, configuration.binanceConfig.secretKey, HmacAlgorithm.HmacSHA256)
    val url = s"${configuration.binanceConfig.url}/sapi/v1/capital/config/getall?$queryString&signature=$signature"
    val response = httpClient.get[Any, Array[Coin]](url, None, Map("X-MBX-APIKEY" -> configuration.binanceConfig.apiKey))

    response map {
      case Left(err) =>
        logger.error(s"getAccountDetail failed: $err")
        None
      case Right(saving) => Some(saving.toList)
    }
  }
}
