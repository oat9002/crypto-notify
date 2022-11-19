package services.crypto

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, Constant, HttpClient}
import models.bitcoin.Utxo

import scala.concurrent.{ExecutionContext, Future}

trait BitcoinService {
  def getAllUtxo(address: String): Future[Option[List[Utxo]]]
}

class BitcoinServiceImpl(configuration: Configuration, httpClient: HttpClient)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends BitcoinService
    with LazyLogging {
  override def getAllUtxo(address: String): Future[Option[List[Utxo]]] = {
    val url = s"${Constant.blockStreamUrl}/address/$address/utxo"
    val response = httpClient.get[List[Utxo]](url)

    response.map {
      case Right(v) => Some(v)
      case Left(ex) =>
        logger.error(s"getAllUtxo failed for $address", ex)
        None
    }
  }
}
