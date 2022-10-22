package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, HttpClient}
import models.bscScan.BscScanResponse
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode
import scala.math.pow

trait BscScanService {
  def getBnbBalance(address: String): Future[Option[BigDecimal]]
  def getTokenBalance(
      contractAddress: String,
      address: String
  ): Future[Option[BigDecimal]]
}

class BscScanServiceImpl(configuration: Configuration, httpClient: HttpClient)(implicit
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends BscScanService
    with LazyLogging {
  val baseUrl: String = configuration.bscScanConfig.map(_.url).getOrElse("")
  val apiKey: String = configuration.bscScanConfig.map(_.apiKey).getOrElse("")

  override def getBnbBalance(address: String): Future[Option[BigDecimal]] = {
    val url =
      s"$baseUrl?module=account&action=balance&address=$address&apikey=$apiKey"
    val response = httpClient.get[BscScanResponse](url)

    response.map {
      case Left(err) =>
        logger.error(s"getBnbBalance failed: $err")
        None
      case Right(x) =>
        if (x.message == "OK") {
          Some(convertFromWei(x.result))
        } else {
          logger.error(s"getBnbBalance failed: ${x.message}")
          None
        }
    }
  }

  override def getTokenBalance(
      contractAddress: String,
      address: String
  ): Future[Option[BigDecimal]] = {
    val url =
      s"$baseUrl?module=account&action=tokenbalance&contractaddress=$contractAddress&address=$address&tag=latest&apikey=$apiKey"
    val response = httpClient.get[BscScanResponse](url)

    response.map {
      case Left(err) =>
        logger.error(s"getTokenBalance failed: $err")
        None
      case Right(x) =>
        if (x.message == "OK") {
          Some(convertFromWei(x.result))
        } else {
          logger.error(s"getTokenBalance failed: ${x.message}")
          None
        }
    }
  }

  private def convertFromWei(value: BigInt): BigDecimal = {
    (BigDecimal(value) / pow(10.0, 18.0)) setScale (6, RoundingMode.HALF_UP)
  }
}
