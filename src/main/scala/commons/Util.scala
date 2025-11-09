package commons

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.ResponseEntity
import commons.Constant.EncryptionAlgorithm

import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.time.chrono.ThaiBuddhistChronology
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.{Base64, Locale}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

object CommonUtil {
  val numberFormatter: NumberFormat =
    NumberFormat.getInstance(new Locale("th", "TH"))
  def formatNumber[T](value: T): String = numberFormatter.format(value)

  extension (value: AnyVal) {
    def format: String = numberFormatter.format(value)
  }

  extension (value: BigDecimal) {
    def format: String = numberFormatter.format(value)
  }

  def generateHMAC(
      message: String,
      key: String,
      encryptionAlgorithm: EncryptionAlgorithm
  ): String = {
    val algorithm = encryptionAlgorithm.toString
    val secret = new SecretKeySpec(key.getBytes(), algorithm)
    val mac = Mac.getInstance(algorithm)

    mac.init(secret)

    val digest = mac.doFinal(message.getBytes())

    digest.map(d => String.format("%02x", d)).mkString("")
  }

  def base64Encode(message: String): String = {
    val text =
      Base64.getEncoder.encodeToString(message.getBytes(StandardCharsets.UTF_8))

    text
  }

  def getFormattedNowDate(
      pattern: String = "E dd MMM YYYY เวลา HH:mm น.",
      isThai: Boolean = true
  ): String = {
    val localDatetime = LocalDateTime.now(ZoneId.of("Asia/Bangkok"))
    val dateFormatter =
      if (isThai) DateTimeFormatter.ofPattern(pattern, new Locale("th", "TH"))
      else DateTimeFormatter.ofPattern(pattern)

    localDatetime.format(
      if (isThai) dateFormatter.withChronology(ThaiBuddhistChronology.INSTANCE)
      else dateFormatter
    )
  }
}

//object HttpResponseUtil {
//  val serializeTimeout: FiniteDuration = 5.seconds
//
//  extension (entity: ResponseEntity) {
//    def toJson(using
//        context: ExecutionContext,
//        actor: ActorSystem[Nothing]
//    ): Future[Option[String]] = entity
//      .toStrict(serializeTimeout)
//      .map(e => e.getData())
//      .map(data => Some(data.utf8String))
//  }
//}
