package services

import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire

import java.time.chrono.ThaiBuddhistChronology
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

trait UserService {
  def getBalanceMessageForLine(userId: String): Future[Option[String]]
}

class UserServiceImpl(satangService: SatangService)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends UserService {
  override def getBalanceMessageForLine(userId: String): Future[Option[String]] = {
    val user = satangService.getUser(userId)
    val currentPrices = satangService.getCryptoPrices

    for {
      u <- user
      currentPrice <- currentPrices
    } yield {
      (u, currentPrice) match {
        case (Some(a), Some(b)) =>
          val noneZeroCryptoBalance =  a.wallets.filter(_._1 != "thb").filter(_._2.availableBalance != 0).map(x => x._1 -> x._2.availableBalance)
          val cryptoBalanceInThb = noneZeroCryptoBalance
            .map(x => x._1 -> (b.find(_.symbol == s"${x._1}_thb").get.lastPrice * x._2).setScale(2, RoundingMode.HALF_UP))

          Some(generateMessage(cryptoBalanceInThb, noneZeroCryptoBalance))
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
