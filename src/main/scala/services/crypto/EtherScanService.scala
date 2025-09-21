package services.crypto

import akka.actor.typed.ActorSystem
import commons.{Configuration, Constant, HttpClient, Logger}
import models.etherScan.EtherScanResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode
import scala.math.pow

trait EtherScanService {
  def getBnbBalance(address: String): Future[Option[BigDecimal]]
  def getTokenBalance(
      contractAddress: String,
      address: String
  ): Future[Option[BigDecimal]]
}

class EtherScanServiceImpl(using configuration: Configuration, httpClient: HttpClient)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext,
    logger: Logger
) extends EtherScanService {
  val baseUrl: String = Constant.etherScanUrl
  val apiKey: String = configuration.etherScanConfig.map(_.apiKey).getOrElse("")

  override def getBnbBalance(address: String): Future[Option[BigDecimal]] = {
    val url =
      s"$baseUrl?chainId=${Constant.bnbMainNetChainId}&module=account&action=balance&address=$address&apikey=$apiKey"
    val response = httpClient.get[EtherScanResponse](url)

    response.map {
      case Left(err) =>
        logger.error(s"getBnbBalance failed: $err")
        None
      case Right(x) =>
        if (x.message == "OK") {
          Some(convertFromWei(BigInt(x.result)))
        } else {
          logger.error(s"getBnbBalance failed: ${x.message}, ${x.result}")
          None
        }
    }
  }

  override def getTokenBalance(
      contractAddress: String,
      address: String
  ): Future[Option[BigDecimal]] = {
    val url =
      s"$baseUrl?chainId=${Constant.bnbMainNetChainId}&module=account&action=tokenbalance&contractaddress=$contractAddress&address=$address&tag=latest&apikey=$apiKey"
    val response = httpClient.get[EtherScanResponse](url)

    response.map {
      case Left(err) =>
        logger.error(s"getTokenBalance failed: $err")
        None
      case Right(x) =>
        if (x.message == "OK") {
          Some(convertFromWei(BigInt(x.result)))
        } else {
          logger.error(s"getTokenBalance failed: ${x.message}, ${x.result}")
          None
        }
    }
  }

  private def convertFromWei(value: BigInt): BigDecimal = {
    (BigDecimal(value) / pow(10.0, 18.0)) setScale (6, RoundingMode.HALF_UP)
  }
}
