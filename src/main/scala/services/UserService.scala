package services

import akka.actor.typed.ActorSystem
import commons.Constant
import models.CryptoBalance
import models.satang.{Ticker => SatangTicker}
import models.binance.{Ticker => BinanceTicker}
import services.contracts.PancakeService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(
      userId: String,
      extWalletAddress: String,
      terraAddress: String
  ): Future[Option[String]]
}

class UserServiceImpl(
    satangService: SatangService,
    bscScanService: BscScanService,
    binanceService: BinanceService,
    terraService: TerraService,
    pancakeService: PancakeService
)(implicit system: ActorSystem[Nothing], context: ExecutionContext)
    extends UserService {
  override def getBalanceMessageForLine(
      userId: String,
      extWalletAddress: String,
      terraAddress: String
  ): Future[Option[String]] = {
    for {
      satangUserOpt <- satangService.getUser(userId)
      satangCurrentPricesOpt <- satangService.getCryptoPrices
      binanceCurrentPricesOpt <- binanceService.getLatestPrice
      extBnbAmountOpt <- bscScanService.getBnbBalance(extWalletAddress)
      extCakeAmountOpt <- bscScanService.getTokenBalance(
        Constant.CakeTokenContractAddress,
        extWalletAddress
      )
      extCakeStakeAmountOpt <- pancakeService.getPancakeStakeBalance(
        extWalletAddress
      )
      binanceOpt <- binanceService.getAllBalance
      terraAccountOpt <- terraService.getAllBalance(terraAddress)
    } yield for {
      satangUser <- satangUserOpt
      satangCurrentPrices <- satangCurrentPricesOpt
      binanceCurrentPrices <- binanceCurrentPricesOpt
      eBnB <- extBnbAmountOpt
      eCake <- extCakeAmountOpt
      eCakeStake <- extCakeStakeAmountOpt
      binance <- binanceOpt
      terra <- terraAccountOpt
    } yield {
      val satangList = satangUser.wallets
        .map(x => CryptoBalance(x._1, x._2.availableBalance))
        .toList
      val externalList = List(
        CryptoBalance("cake", eCake + eCakeStake),
        CryptoBalance("bnb", eBnB)
      )
      val mergedPair = (satangList ++ binance ++ externalList ++ terra)
        .groupBy(_.symbol)
        .map { case (k, v) =>
          CryptoBalance(k, v.map(_.balance).sum)
        }
        .toList
      val noneZeroCryptoBalance = mergedPair
        .filter(x => x.symbol != "thb" && x.balance != 0)
        .sortBy(_.symbol)
      val cryptoBalanceInThb = noneZeroCryptoBalance
        .map(x =>
          getCryptoPriceInThb(x, satangCurrentPrices, binanceCurrentPrices)
        )
      val allBalanceIntThb = satangList
        .filter(_.symbol == "thb")
        .map(x => CryptoBalance("fiat money", x.balance))
        .concat(cryptoBalanceInThb)
        .sortBy(_.symbol)

      generateMessage(allBalanceIntThb, noneZeroCryptoBalance)
    }
  }

  private def getCryptoPriceInThb(
      balance: CryptoBalance,
      satangPrices: List[SatangTicker],
      binancePrices: List[BinanceTicker]
  ): CryptoBalance = {
    val lastThbPrice = satangPrices
      .filter(_.symbol != "luna_thb")
      .find(_.symbol == s"${balance.symbol}_thb")
      .map(_.lastPrice)
      .getOrElse {
        binancePrices
          .find(_.symbol.toLowerCase() == s"${balance.symbol}busd")
          .map(_.price) match {
          case Some(busd) =>
            satangPrices
              .find(_.symbol == "busd_thb")
              .map(_.lastPrice)
              .getOrElse(BigDecimal(0)) * busd
          case None =>
            binancePrices
              .find(_.symbol.toLowerCase() == s"${balance.symbol}btc")
              .map(_.price) match {
              case Some(btc) =>
                satangPrices
                  .find(_.symbol == "btc_thb")
                  .map(_.lastPrice)
                  .getOrElse(BigDecimal(0)) * btc
              case None => BigDecimal(0)
            }
        }
      }

    CryptoBalance(
      balance.symbol,
      (balance.balance * lastThbPrice).setScale(2, RoundingMode.HALF_UP)
    )
  }

  private def generateMessage(
      allBalanceInThb: List[CryptoBalance],
      cryptoBalance: List[CryptoBalance]
  ): String = {
    import commons.CommonUtil._

    val date = getFormattedNowDate() + "\n"
    val sumCurrentBalanceThb =
      s"จำนวนเงินทั้งหมด: ${allBalanceInThb.map(_.balance).sum.format} บาท\n"
    val balanceThb = allBalanceInThb
      .map(x => s"${x.symbol}: ${x.balance.format} บาท")
      .mkString("\n")
    val balance =
      cryptoBalance.map(x => s"${x.symbol}: ${x.balance.format}").mkString("\n")

    "\n"
      .concat(date)
      .concat(sumCurrentBalanceThb)
      .concat(balanceThb)
      .concat("\n\n")
      .concat(balance)
  }
}
