package helpers

import scala.math.BigDecimal.RoundingMode
import scala.math.pow

trait TerraHelper {
  def denomToSymbol(denom: String): Option[String]
  def convertRawAmount(amount: String): BigDecimal
}

class TerraHelperImpl extends TerraHelper {
  def denomToSymbol(denom: String): Option[String] = {
    denom match {
      case "uusd" => Some("ust")
      case _ => None
    }
  }

  def convertRawAmount(amountStr: String): BigDecimal = {
    val amount = amountStr.toLong

    (BigDecimal(amount) / pow(10.0, 6.0)).setScale(6, RoundingMode.HALF_UP)
  }
}
