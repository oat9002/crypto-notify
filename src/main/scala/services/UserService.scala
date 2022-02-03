package services

import akka.actor.typed.ActorSystem
import commons.Constant

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]]
}

class UserServiceImpl(satangService: SatangService, bscScanService: BscScanService, binanceService: BinanceService)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends UserService {
  override def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]] = {
    val userFuture = satangService.getUser(userId)
    val currentPricesFuture = satangService.getCryptoPrices
    val extBnbAmountFuture = bscScanService.getBnbBalance(extWalletAddress)
    val extCakeAmountFuture = bscScanService.getTokenBalance(Constant.CakeTokenContractAddress, extWalletAddress)
    val extCakeStakeAmountFuture = bscScanService.getTokenBalance(Constant.CakeTokenStakeContractAddress, extWalletAddress)
    val extBetaAmountFuture = bscScanService.getTokenBalance(Constant.BetaTokenContractAddress, extWalletAddress)
    val binanceSavingFuture = binanceService.getSaving

    for {
      user <- userFuture
      currentPrices <- currentPricesFuture
      extBnbAmount <- extBnbAmountFuture
      extCakeAmount <- extCakeAmountFuture
      extCakeStakeAmount <- extCakeStakeAmountFuture
      extBetaAmount <- extBetaAmountFuture
      binanceSaving <- binanceSavingFuture
    } yield {
      (user, currentPrices, extBnbAmount, extCakeAmount, extCakeStakeAmount, extBetaAmount, binanceSaving) match {
        case (Some(u), Some(cp), Some(eBnB), Some(eCake), Some(eCakeStake), Some(eBetaAmount), Some(binSaving)) =>
          val satangMap =  u.wallets.map(x => x._1 -> x._2.availableBalance)
          val binanceMap = binSaving.positionAmountVos.map(x => x.asset.toLowerCase() -> x.amount).toMap
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
            .map(x => x._1 -> (cp.find(_.symbol == s"${x._1}_thb").get.lastPrice * x._2).setScale(2, RoundingMode.HALF_UP))
          val allBalanceIntThb = satangMap.filter(_._1 == "thb")
            .map("fiat money" -> _._2)
            .concat(cryptoBalanceInThb)

          Some(generateMessage(allBalanceIntThb, noneZeroCryptoBalance))
        case _ => None
      }
    }
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
