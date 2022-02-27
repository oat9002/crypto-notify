package services

import akka.actor.typed.ActorSystem
import commons.Constant
import models.satang.{Ticker => SatangTicker}
import models.binance.{Ticker => BinanceTicker}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]]
}

class UserServiceImpl(satangService: SatangService, bscScanService: BscScanService, binanceService: BinanceService)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends UserService {
  override def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]] = {
    for {
      satangUser <- satangService.getUser(userId)
      satangCurrentPrices <- satangService.getCryptoPrices
      binanceCurrentPrices <- binanceService.getLatestPrice
      extBnbAmount <- bscScanService.getBnbBalance(extWalletAddress)
      extCakeAmount <- bscScanService.getTokenBalance(Constant.CakeTokenContractAddress, extWalletAddress)
      extCakeStakeAmount <- bscScanService.getTokenBalance(Constant.CakeTokenStakeContractAddress, extWalletAddress)
      extBetaAmount <- bscScanService.getTokenBalance(Constant.BetaTokenContractAddress, extWalletAddress)
      binanceSaving <- binanceService.getSaving
      binanceAccount <- binanceService.getAccountDetail
    } yield {
      (satangUser, satangCurrentPrices, binanceCurrentPrices, extBnbAmount, extCakeAmount, extCakeStakeAmount, extBetaAmount, binanceSaving, binanceAccount) match {
        case (Some(u), Some(cp), Some(bcp), Some(eBnB), Some(eCake), Some(eCakeStake), Some(eBetaAmount), Some(binSaving), Some(binAccount)) =>
          val satangMap =  u.wallets.map(x => x._1 -> x._2.availableBalance)
          val binanceMap = (binSaving.positionAmountVos.map(x => x.asset.toLowerCase() -> x.amount) ++ binAccount.filter(_.free != 0).map(x => x.coin.toLowerCase() -> x.free)).groupBy(_._1).map {
            case (k, v) => k -> v.map(_._2).sum
          }
          val externalMap = Map(
            "cake" -> (eCake + eCakeStake),
            "bnb" -> eBnB,
            "beta" -> eBetaAmount
          )
          val mergedPair = (satangMap.toList ++ binanceMap.toList ++ externalMap.toList).groupBy(_._1).map {
            case (k, v) => k -> v.map(_._2).sum
          }
          val noneZeroCryptoBalance = mergedPair.filter(x => x._1 != "thb" && x._2 != 0)
          val cryptoBalanceInThb = noneZeroCryptoBalance
            .map(x => getCryptoPriceInThb(x, cp, bcp))
          val allBalanceIntThb = satangMap.filter(_._1 == "thb")
            .map("fiat money" -> _._2)
            .concat(cryptoBalanceInThb)

          Some(generateMessage(allBalanceIntThb, noneZeroCryptoBalance))
        case _ => None
      }
    }
  }

  private def getCryptoPriceInThb(balance: (String, BigDecimal), satangPrices: List[SatangTicker], binancePrices: List[BinanceTicker]): (String, BigDecimal) = {
    val lastThbPrice = satangPrices.find(_.symbol == s"${balance._1}_thb").map(_.lastPrice).getOrElse {
      val btcBinance = binancePrices.find(_.symbol.toLowerCase() == s"${balance._1}btc").map(_.price).get

      satangPrices.find(_.symbol == "btc_thb").map(_.lastPrice).get * btcBinance
    }

    balance._1 -> (balance._2 * lastThbPrice).setScale(2, RoundingMode.HALF_UP)
  }

  private def generateMessage(allBalanceInThb: Map[String, BigDecimal], cryptoBalance: Map[String, BigDecimal]): String = {
    import commons.CommonUtil._

    val date = getFormattedNowDate() + "\n"
    val sumCurrentBalanceThb = s"จำนวนเงินทั้งหมด: ${allBalanceInThb.values.sum.format} บาท\n"
    val balanceThb = allBalanceInThb.map(x => s"${x._1}: ${x._2.format} บาท").mkString("\n")
    val balance = cryptoBalance.map(x => s"${x._1}: ${x._2.format}").mkString("\n")

    "\n".concat(date).concat(sumCurrentBalanceThb).concat(balanceThb).concat("\n\n").concat(balance)
  }
}
