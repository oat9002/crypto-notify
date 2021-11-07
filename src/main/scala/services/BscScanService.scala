package services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import commons.HttpResponseUtil.ToJsonString
import commons.JsonUtil.JsonDeserialize
import models.bscScan.BscScanResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode
import scala.math.pow

trait BscScanService {
  def getBnbBalance(address: String): Future[Option[BigDecimal]]
  def getTokenBalance(contractAddress: String, address: String): Future[Option[BigDecimal]]
}

class BscScanServiceImpl(configuration: Configuration)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends BscScanService with LazyLogging {
  override def getBnbBalance(address: String): Future[Option[BigDecimal]] = {
    val url = s"${configuration.bscScanConfig.url}?module=account&action=balance&address=$address&apikey=${configuration.bscScanConfig.apiKey}"
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = url
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toJson
      case HttpResponse(_, _, entity, _) => Future.successful(None)
      case _ => Future.successful(None)
    }.map {
      case Some(x) =>
        val res = x.toObject[BscScanResponse]
        if (res.message == "OK") {
          Some(convertFromWei(res.result))
        } else {
          logger.error(s"getBnbBalance failed: ${res.message}")
          None
        }
      case _ => None
    }
  }

  override def getTokenBalance(contractAddress: String, address: String): Future[Option[BigDecimal]] = {
    val url = s"${configuration.bscScanConfig.url}?module=account&action=tokenbalance&contractaddress=$contractAddress&address=$address&tag=latest&apikey=${configuration.bscScanConfig.apiKey}"
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = url
    ))

    response.flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.toJson
      case HttpResponse(_, _, entity, _) => Future.successful(None)
      case _ => Future.successful(None)
    }.map {
      case Some(x) =>
        val res = x.toObject[BscScanResponse]
        if (res.message == "OK") {
          Some(convertFromWei(res.result))
        } else {
          logger.error(s"getTokenBalance failed: ${res.message}")
          None
        }
      case _ => None
    }
  }

  private def convertFromWei(value: BigInt): BigDecimal = {
    (BigDecimal(value) / pow(10.0, 18.0))setScale(6, RoundingMode.HALF_UP)
  }
}
