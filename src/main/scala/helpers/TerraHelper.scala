package helpers

import scala.math.BigDecimal.RoundingMode
import scala.math.pow

trait TerraHelper {
  def denomToSymbolClassic(denom: String): Option[String]
  def denomToSymbol(denom: String): Option[String]
  def convertRawAmount(amount: Long): BigDecimal
}

class TerraHelperImpl extends TerraHelper {
  override def denomToSymbolClassic(denom: String): Option[String] = {
    denom match {
      case "uusd"  => Some("ustc")
      case "uluna" => Some("lunc")
      case _       => None
    }
  }

  override def denomToSymbol(denom: String): Option[String] = {
    denom match {
      case "uusd"  => Some("ust")
      case "uluna" => Some("luna")
      case _       => None
    }
  }

  override def convertRawAmount(amount: Long): BigDecimal = {
    (BigDecimal(amount) / pow(10.0, 6.0)).setScale(6, RoundingMode.HALF_UP)
  }
}
