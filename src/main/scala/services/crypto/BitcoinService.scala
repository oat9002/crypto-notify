package services.crypto

import akka.actor.typed.ActorSystem
import commons.{Configuration, Constant, HttpClient, Logger}
import helpers.BitcoinHelper
import models.bitcoin.Utxo

import scala.concurrent.{ExecutionContext, Future}

trait BitcoinService {
  def getAllUtxo(address: String): Future[Option[List[Utxo]]]
  def getBitcoinBalance(addresses: List[String]): Future[Option[BigDecimal]]
}

class BitcoinServiceImpl(using configuration: Configuration, httpClient: HttpClient, logger: Logger)(using
                                                                                                     system: ActorSystem[Nothing],
                                                                                                     context: ExecutionContext
) extends BitcoinService {
  override def getAllUtxo(address: String): Future[Option[List[Utxo]]] = {
    val url = s"${Constant.blockStreamUrl}/address/$address/utxo"
    val response = httpClient.get[List[Utxo]](url)

    response.map {
      case Right(v) => Some(v)
      case Left(ex) =>
        logger.error(s"getAllUtxo failed for $address, $ex")
        None
    }
  }

  override def getBitcoinBalance(addresses: List[String]): Future[Option[BigDecimal]] = {
    val allUtxoes = Future.sequence(addresses.map(getAllUtxo)).map(_.flatten.flatten)
    val balance =
      allUtxoes.map(_.map(_.value).sum).map(b => Option(BitcoinHelper.fromSatoshiToBitcoin(b)))

    balance
  }
}
