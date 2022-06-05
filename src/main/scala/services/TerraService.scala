package services

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.JsonUtil.JsonSerialize
import commons.{CommonUtil, Configuration, Constant, HttpClient}
import helpers.TerraHelper
import models.CryptoBalance
import models.terra.{
  Balance,
  ExchangeRate,
  QueryResult,
  RawWallet,
  Wallet,
  aUstBalance
}

import scala.concurrent.{ExecutionContext, Future}

trait TerraService {
  def getWalletBalance(address: String): Future[Option[Wallet]]
  def getAllBalance(address: String): Future[Option[List[CryptoBalance]]]
  def getaUstBalance(address: String): Future[Option[BigDecimal]]
  def getaUstExchangeRate(): Future[Option[BigDecimal]]
}

class TerraServiceImpl(
    configuration: Configuration,
    httpClient: HttpClient,
    terraHelper: TerraHelper
)(implicit system: ActorSystem[Nothing], context: ExecutionContext)
    extends TerraService
    with LazyLogging {
  override def getWalletBalance(address: String): Future[Option[Wallet]] = {
    val classicUrl =
      s"${configuration.terraConfig.url}/cosmos/bank/v1beta1/balances/$address"
    val twoPointOUrl =
      s"${configuration.terraConfig.twoPointOUrl}/cosmos/bank/v1beta1/balances/$address"
    val classicResponse = httpClient.get[RawWallet](classicUrl)
    val twoPointOResponse = httpClient.get[RawWallet](twoPointOUrl)

    classicResponse zip twoPointOResponse map {
      case (Right(classicWallet), Right(twoPointOWallet)) =>
        val classicBalance = classicWallet.balances.map { x =>
          val symbol = terraHelper.denomToSymbolClassic(x.denom)
          val amount = terraHelper.convertRawAmount(x.amount)

          symbol -> amount
        }
        val twoPointOBalance = twoPointOWallet.balances.map { x =>
          val symbol = terraHelper.denomToSymbol(x.denom)
          val amount = terraHelper.convertRawAmount(x.amount)

          symbol -> amount
        }

        if (classicBalance.exists(_._1.isEmpty)) {
          logger.warn(
            s"Some denoms aren't define on terra classic, rawWallet:${classicBalance.toJson}"
          )
        }

        if (twoPointOBalance.exists(_._1.isEmpty)) {
          logger.warn(
            s"Some denoms aren't define on terra 2.0, rawWallet:${twoPointOBalance.toJson}"
          )
        }

        val allBalance = classicBalance
          .filter(_._1.nonEmpty)
          .map(x => Balance(x._1.get, x._2))
          .toList ++ twoPointOBalance
          .filter(_._1.nonEmpty)
          .map(x => Balance(x._1.get, x._2))
          .toList

        Some(Wallet(balances = allBalance))
      case (Left(classicErr), Left(twoPointOErr)) =>
        logger.error(s"getBalance failed", classicErr, twoPointOErr)
        None
      case (Left(err), _) =>
        logger.error(s"getBalance failed", err)
        None
      case (_, Left(err)) =>
        logger.error(s"getBalance failed", err)
        None
    }
  }

  override def getaUstBalance(address: String): Future[Option[BigDecimal]] = {
    val queryMsg =
      CommonUtil.base64Encode("{\"balance\":{\"address\":\"" + address + "\"}}")
    val url =
      s"${configuration.terraConfig.url}/terra/wasm/v1beta1/contracts/${Constant.aUstContractAddress}/store?query_msg=$queryMsg"
    val response = httpClient.get[QueryResult[aUstBalance]](url)

    response map {
      case Left(err) =>
        logger.error(s"getaUstBalance failed, err: $err")
        None
      case Right(v) => Some(terraHelper.convertRawAmount(v.queryResult.balance))
    }
  }

  override def getaUstExchangeRate(): Future[Option[BigDecimal]] = {
    val url =
      s"${configuration.terraConfig.url}/terra/wasm/v1beta1/contracts/${Constant.anchorMarketContractAddress}/store?query_msg=eyJlcG9jaF9zdGF0ZSI6e319"
    val response = httpClient.get[QueryResult[ExchangeRate]](url)

    response map {
      case Left(err) =>
        logger.error(s"getsUstExchange failed, err: $err")
        None
      case Right(v) => Some(v.queryResult.exchangeRate)
    }
  }

  override def getAllBalance(
      address: String
  ): Future[Option[List[CryptoBalance]]] = {
    for {
      walletBalanceOpt <- getWalletBalance(address)
      aUstExchangeRateOpt <- getaUstExchangeRate()
      aUstBalanceOpt <- getaUstBalance(address)
    } yield for {
      walletBalance <- walletBalanceOpt
      aUstExchangeRate <- aUstExchangeRateOpt
      aUstBalance <- aUstBalanceOpt
    } yield {
      val newBalances = walletBalance.balances.map {
        case Balance(symbol, balance) if symbol == "ust" =>
          Balance(symbol, balance + (aUstBalance * aUstExchangeRate))
        case x => x
      }

      newBalances.map(x => CryptoBalance(symbol = x.symbol, balance = x.amount))
    }
  }
}
