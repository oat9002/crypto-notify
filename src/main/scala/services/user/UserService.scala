package services.user

import akka.actor.typed.ActorSystem
import commons.Constant
import models.CryptoBalance
import models.binance.Ticker as BinanceTicker
import models.satang.Ticker as SatangTicker
import services.crypto.contracts.PancakeService
import services.crypto.*

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(
      userId: String,
      bscAddress: Option[String],
      terraAddress: Option[String],
      bitcoinAddress: Option[List[String]]
  ): Future[Option[String]]
}

class UserServiceImpl(using
    satangService: SatangService,
    bscScanService: BscScanService,
    binanceService: BinanceService,
    terraService: TerraService,
    pancakeService: PancakeService,
    bitcoinService: BitcoinService
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends UserService {
  override def getBalanceMessageForLine(
      userId: String,
      bscAddress: Option[String],
      terraAddress: Option[String],
      bitcoinAddress: Option[List[String]]
  ): Future[Option[String]] = {
    val satangUserF = satangService.getUser(userId)
    val satangCurrentPricesF = satangService.getCryptoPrices
    val binanceCurrentPricesF = binanceService.getLatestPrice
    val extBnbAmountF = bscAddress
      .map(bscScanService.getBnbBalance)
      .getOrElse(Future.successful(None))
    val extCakeAmountF = bscAddress
      .map(address =>
        bscScanService.getTokenBalance(
          Constant.CakeTokenContractAddress,
          address
        )
      )
      .getOrElse(Future.successful(None))
    val extCakeStakeAmountF = bscAddress
      .map(pancakeService.getPancakeStakeBalance)
      .getOrElse(Future.successful(None))
    val binanceF = binanceService.getAllBalance
    val terraAccountF = terraAddress
      .map(terraService.getAllBalance)
      .getOrElse(Future.successful(None))
    val bitcoinExtBalanceF = bitcoinAddress
      .map(bitcoinService.getBitcoinBalance)
      .getOrElse(Future.successful(None))

    for {
      satangUser <- satangUserF
      satangCurrentPrices <- satangCurrentPricesF
      binanceCurrentPrices <- binanceCurrentPricesF
      extBnbAmount <- extBnbAmountF
      extCakeAmount <- extCakeAmountF
      extCakeStakeAmount <- extCakeStakeAmountF
      binance <- binanceF
      terraAccount <- terraAccountF
      bitcoinExtBalance <- bitcoinExtBalanceF
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
        CryptoBalance("bnb", extBnbAmount.getOrElse(BigDecimal(0))),
        CryptoBalance("btc", bitcoinExtBalance.getOrElse(BigDecimal(0)))
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
    val btcToThb = satangPrices
      .find(_.symbol == "btc_thb")
      .map(_.lastPrice)
      .getOrElse(BigDecimal(0))

    val lastThbPrice = balance.symbol match {
      case "wbeth" =>
        val wbethToEth = binancePrices
          .find(_.symbol.toLowerCase() == "wbetheth")
          .map(_.price)
        val ethToBtc = binancePrices
          .find(_.symbol.toLowerCase() == "ethbtc")
          .map(_.price)

        (wbethToEth, ethToBtc) match {
          case (Some(toEth), Some(toBtc)) =>
            toEth * toBtc * btcToThb
          case _ => BigDecimal(0)
        }
      case _ =>
        satangPrices
          .filter(_.symbol != "luna_thb")
          .find(_.symbol == s"${balance.symbol}_thb")
          .map(_.lastPrice)
          .getOrElse {
            val usdtToThb = satangPrices
              .find(_.symbol == "usdt_thb")
              .map(_.lastPrice)
              .getOrElse(BigDecimal(0))

            binancePrices
              .find(_.symbol.toLowerCase() == s"${balance.symbol}usdt")
              .map(_.price)
              .map(_ * usdtToThb)
              .getOrElse {
                binancePrices
                  .find(_.symbol.toLowerCase() == s"${balance.symbol}btc")
                  .map(_.price)
                  .map(_ * btcToThb)
                  .getOrElse(BigDecimal(0))
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
    val sortedAllBalanceInThb = allBalanceInThb.sortWith((x, y) => x.balance > y.balance)
    val sortedCryptoBalance = sortedAllBalanceInThb
      .map(_.symbol)
      .distinct
      .map(x => cryptoBalance.find(_.symbol == x).getOrElse(CryptoBalance(x, 0)))
      .filter(_.balance != 0)

    val date = getFormattedNowDate() + "\n"
    val sumCurrentBalanceThb =
      s"จำนวนเงินทั้งหมด: ${sortedAllBalanceInThb.map(_.balance).sum.format} บาท\n"
    val balanceThb = sortedAllBalanceInThb
      .map(x => s"${x.symbol}: ${x.balance.format} บาท")
      .mkString("\n")
    val balance =
      sortedCryptoBalance.map(x => s"${x.symbol}: ${x.balance.format}").mkString("\n")

    "\n"
      .concat(date)
      .concat(sumCurrentBalanceThb)
      .concat(balanceThb)
      .concat("\n\n")
      .concat(balance)
  }
}
