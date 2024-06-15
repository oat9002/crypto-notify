package models.satang

import io.circe.*
import io.circe.generic.semiauto.*

case class Ticker(
    askPrice: BigDecimal,
    bidPrice: BigDecimal,
    closeTime: BigInt,
    count: Int,
    firstId: BigInt,
    highPrice: BigDecimal,
    lastId: BigInt,
    lastPrice: BigDecimal,
    lastQty: BigDecimal,
    lowPrice: BigDecimal,
    openPrice: BigDecimal,
    openTime: BigInt,
    prevClosePrice: BigDecimal,
    priceChange: BigDecimal,
    priceChangePercent: BigDecimal,
    quoteVolume: BigDecimal,
    symbol: String,
    volume: BigDecimal,
    weightedAvgPrice: BigDecimal
)
object Ticker {
  given Encoder[Ticker] = deriveEncoder[Ticker]
  given Decoder[Ticker] = deriveDecoder[Ticker]
}
