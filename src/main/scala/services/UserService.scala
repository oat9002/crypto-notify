package services

import akka.actor.typed.ActorSystem
import commons.Constant

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]]
}

class UserServiceImpl(satangService: SatangService, bscScanService: BscScanService)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends UserService {
  override def getBalanceMessageForLine(userId: String, extWalletAddress: String): Future[Option[String]] = {
    val userFuture = satangService.getUser(userId)
    val currentPricesFuture = satangService.getCryptoPrices
    val extBnbAmountFuture = bscScanService.getBnbBalance(extWalletAddress)
    val extCakeAmountFuture = bscScanService.getTokenBalance(Constant.CakeTokenContractAddress, extWalletAddress)
    val extCakeStakeAmountFuture = bscScanService.getTokenBalance(Constant.CakeTokenStakeContractAddress, extWalletAddress)

    for {
      user <- userFuture
      currentPrices <- currentPricesFuture
      extBnbAmount <- extBnbAmountFuture
      extCakeAmount <- extCakeAmountFuture
      extCakeStakeAmount <- extCakeStakeAmountFuture
    } yield {
      (user, currentPrices, extBnbAmount, extCakeAmount, extCakeStakeAmount) match {
        case (Some(u), Some(cp), Some(eBnB), Some(eCake), Some(eCakeStake)) =>
          val pairMap =  u.wallets.map(x => x._1 -> x._2.availableBalance)
          val noneZeroCryptoBalance = pairMap
            .map {
              case ("bnb", availableBalance) => ("bnb", availableBalance + eBnB)
              case ("cake", availableBalance) => ("cake", availableBalance + eCake + eCakeStake)
              case (pair, availableBalance) => (pair, availableBalance)
            }
            .filter(x => x._1 != "thb" && x._2 != 0)

          val cryptoBalanceInThb = noneZeroCryptoBalance
            .map(x => x._1 -> (cp.find(_.symbol == s"${x._1}_thb").get.lastPrice * x._2).setScale(2, RoundingMode.HALF_UP))
          val allBalanceIntThb = pairMap.filter(_._1 == "thb")
            .map("fiat money" -> _._2)
            .concat(cryptoBalanceInThb)

          Some(generateMessage(allBalanceIntThb, noneZeroCryptoBalance))
        case _ => None
      }
    }
  }

  private def generateMessage(cryptoBalanceThb: Map[String, BigDecimal], cryptoBalance: Map[String, BigDecimal]): String = {
    import commons.CommonUtil._

    val date = getFormattedNowDate() + "\n"
    val sumCurrentBalanceThb = s"จำนวนเงินทั้งหมด: ${cryptoBalanceThb.values.sum.format} บาท\n"
    val balanceThb = cryptoBalanceThb.map(x => s"${x._1}: ${x._2.format} บาท").mkString("\n")
    val balance = cryptoBalance.map(x => s"${x._1}: ${x._2.format}").mkString("\n")

    "\n".concat(date).concat(sumCurrentBalanceThb).concat(balanceThb).concat("\n\n").concat(balance)
  }
}
