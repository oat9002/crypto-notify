package services

import akka.actor.ActorSystem
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

class UserServiceImpl(implicit actor: ActorSystem, context: ExecutionContext) extends UserService {
  lazy val satangService: SatangService = wire[SatangServiceImpl]

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
          val f = generateMessage(cryptoBalanceInThb, noneZeroCryptoBalance)

          Some(generateMessage(cryptoBalanceInThb, noneZeroCryptoBalance))
        case _ => None
      }
    }
  }

  private def generateMessage(cryptoBalanceThb: Map[String, BigDecimal], cryptoBalance: Map[String, BigDecimal]): String = {
    val localDatetime = LocalDateTime.now(ZoneId.of("Asia/Bangkok"))
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd MMM YYYY HH:mm", new Locale("th", "TH")).withChronology(ThaiBuddhistChronology.INSTANCE)
    val date = localDatetime.format(dateFormatter) + "\n"
    val sumCurrentBalanceThb = s"จำนวนเงินทั้งหมด: ${cryptoBalanceThb.values.sum}\n"
    val balanceThb = cryptoBalanceThb.map(x => s"${x._1}: ${x._2}").mkString("\n")
    val balance = cryptoBalance.map(x => s"${x._1}: ${x._2}").mkString("\n")

    date.concat(sumCurrentBalanceThb).concat(balanceThb).concat("\n\n").concat(balance)
  }
}
