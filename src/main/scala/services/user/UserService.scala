package services.user

import akka.actor.typed.ActorSystem
import commons.Constant
import models.CryptoBalance
import models.binance.Ticker as BinanceTicker
import models.satang.{User, Ticker as SatangTicker}
import services.user.UserService
import services.crypto.contracts.PancakeService
import services.crypto.{BinanceService, BscScanService, SatangService, TerraService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(
      userId: String,
      bscAddress: Option[String],
      terraAddress: Option[String]
  ): Future[Option[String]]
}

class UserServiceImpl(
    satangService: SatangService,
    bscScanService: BscScanService,
    binanceService: BinanceService,
    terraService: TerraService,
    pancakeService: PancakeService
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends UserService {
  override def getBalanceMessageForLine(
      userId: String,
      bscAddress: Option[String],
      terraAddress: Option[String]
  ): Future[Option[String]] = {
    for {
      satangUser <- satangService.getUser(userId)
      satangCurrentPrices <- satangService.getCryptoPrices
      binanceCurrentPrices <- binanceService.getLatestPrice
      extBnbAmount <- bscAddress
        .map(bscScanService.getBnbBalance)
        .getOrElse(Future.successful(None))
      extCakeAmount <- bscAddress
        .map(address =>
          bscScanService.getTokenBalance(
            Constant.CakeTokenContractAddress,
            address
          )
        )
        .getOrElse(Future.successful(None))
      extCakeStakeAmount <- bscAddress
        .map(pancakeService.getPancakeStakeBalance)
        .getOrElse(Future.successful(None))
      binance <- binanceService.getAllBalance
      terraAccount <- terraAddress
        .map(terraService.getAllBalance)
        .getOrElse(Future.successful(None))
    } yield {
      val satangList = satangUser
        .map(s => s.wallets.map(x => CryptoBalance(x._1, x._2.availableBalance)).toList)
        .getOrElse(List[CryptoBalance]())
      val externalList = List(
        CryptoBalance(
          "cake",
          extCakeAmount.getOrElse(BigDecimal(0)) + extCakeStakeAmount.getOrElse(
            BigDecimal(0)
          )
        ),
        CryptoBalance("bnb", extBnbAmount.getOrElse(BigDecimal(0)))
      )
      val mergedPair = (satangList ++ binance.getOrElse(
        List[CryptoBalance]()
      ) ++ externalList ++ terraAccount.getOrElse(List[CryptoBalance]()))
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
          getCryptoPriceInThb(
            x,
            satangCurrentPrices.getOrElse(List[SatangTicker]()),
            binanceCurrentPrices.getOrElse(List[BinanceTicker]())
          )
        )
      val allBalanceIntThb = satangList
        .filter(_.symbol == "thb")
        .map(x => CryptoBalance("fiat money", x.balance))
        .concat(cryptoBalanceInThb)
        .sortBy(_.symbol)

      if (allBalanceIntThb.isEmpty && noneZeroCryptoBalance.isEmpty) {
        None
      } else {
        Some(generateMessage(allBalanceIntThb, noneZeroCryptoBalance))
      }
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
    import commons.CommonUtil.*

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
