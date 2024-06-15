package models.binance

import io.circe.*
import io.circe.generic.semiauto.*

case class Ticker(symbol: String, price: BigDecimal)
object Ticker {
  given Encoder[Ticker] = deriveEncoder[Ticker]
  given Decoder[Ticker] = deriveDecoder[Ticker]
}
