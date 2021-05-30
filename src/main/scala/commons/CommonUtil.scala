package commons

import java.text.NumberFormat
import java.util.Locale

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
}
