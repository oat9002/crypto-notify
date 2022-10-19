package models.binance

import io.circe._
import io.circe.generic.semiauto._

case class Ticker(symbol: String, price: BigDecimal)
object Ticker {
  given Encoder[Ticker] = deriveEncoder[Ticker]
  given Decoder[Ticker] = deriveDecoder[Ticker]
}
