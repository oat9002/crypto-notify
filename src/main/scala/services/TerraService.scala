package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.JsonUtil.JsonSerialize
import commons.{Configuration, Constant, HttpClient}
import helpers.TerraHelper
import models.terra.{Balance, ExchangeRate, QueryResult, RawWallet, Wallet}

import scala.concurrent.{ExecutionContext, Future}

trait TerraService {
  def getBalance(address: String): Future[Option[Wallet]]
  def getaUstBalance(address: String): Future[Option[BigDecimal]]
  def getaUstExchangeRate(): Future[Option[BigDecimal]]
}

class TerraServiceImpl(configuration: Configuration, httpClient: HttpClient, terraHelper: TerraHelper)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends TerraService with LazyLogging {
  override def getBalance(address: String): Future[Option[Wallet]] = {
    val url = s"${configuration.terraConfig.url}/cosmos/bank/v1beta1/balances/$address"
    val response = httpClient.get[RawWallet](url)

    response map {
      case Left(err) =>
        logger.error(s"getBalance failed, err: $err")
        None
      case Right(rawWallet) =>
        val balance = rawWallet.balances.map { x =>
          val symbol = terraHelper.denomToSymbol(x.denom)
          val amount = terraHelper.convertRawAmount(x.amount)

          symbol -> amount
        }

        if (balance.exists(_._1.isEmpty)) {
          logger.error(s"Some denoms aren't define, rawWallet:${rawWallet.toJson}")
          None
        } else {
          Some(Wallet(balances = balance.map(x => Balance(x._1.get, x._2)).toList))
        }
    }
  }

  override def getaUstBalance(address: String): Future[Option[BigDecimal]] = {

  }

  override def getaUstExchangeRate(): Future[Option[BigDecimal]] = {
    val url = s"${configuration.terraConfig.url}/terra/wasm/v1beta1/contracts/${Constant.anchorMarketContractAddress}/store?query_msg=eyJlcG9jaF9zdGF0ZSI6e319"
    val response = httpClient.get[QueryResult[ExchangeRate]](url)

    response map {
      case Left(err) =>
        logger.error(s"getsUstExchange failed, err: $err")
        None
      case Right(v) => Some(v.queryResult.exchangeRate)
    }
  }
}
