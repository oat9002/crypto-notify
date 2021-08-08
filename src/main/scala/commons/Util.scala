package commons

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ResponseEntity
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.text.NumberFormat
import java.time.{LocalDateTime, ZoneId}
import java.time.chrono.ThaiBuddhistChronology
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait Format {
  val numberFormatter: NumberFormat = NumberFormat.getInstance(new Locale("th", "TH"))

  def formatNumber[T](value: T): String = numberFormatter.format(value)
}

object CommonUtil {
  implicit class FormatNumberAnyVal(value: AnyVal) extends Format {
    def format: String = numberFormatter.format(value)
  }

  implicit class FormatBigDecimal(value: BigDecimal) extends Format {
    def format: String = numberFormatter.format(value)
  }

  def generateHMAC512(message: String, key: String): String = {
    val algorithm = "HmacSHA512"
    val secret = new SecretKeySpec(key.getBytes(), algorithm)
    val mac = Mac.getInstance(algorithm)

    mac.init(secret)

    val digest = mac.doFinal(message.getBytes())

    digest.map(d => String.format("%02x", d)).mkString("")
  }

  def getFormattedNowDate(pattern: String = "E dd MMM YYYY เวลา HH:mm น.", isThai: Boolean = true): String = {
    val localDatetime = LocalDateTime.now(ZoneId.of("Asia/Bangkok"))
    val dateFormatter = if (isThai) DateTimeFormatter.ofPattern(pattern, new Locale("th", "TH")) else DateTimeFormatter.ofPattern(pattern)

    localDatetime.format(if (isThai) dateFormatter.withChronology(ThaiBuddhistChronology.INSTANCE) else dateFormatter)
  }
}

object JsonUtil {
  private val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build()

  implicit class JsonSerialized(obj: Any) {
    def toJson: String = mapper.writeValueAsString(obj)
  }

  implicit class JsonDeserialize(content: String) {
    def toObject[T](clazz: Class[T]): T = mapper.readValue(content, clazz)
  }
}

object HttpResponseUtil {
  val serializeTimeout: FiniteDuration = 5.seconds

  implicit class ToJsonString(entity: ResponseEntity) {
    def toJson(implicit context: ExecutionContext, actor: ActorSystem[Nothing]): Future[Option[String]] = entity.toStrict(serializeTimeout).map(e => e.getData()).map(data => Some(data.utf8String))
  }
}

