package commons

import io.circe.*
import io.circe.generic.semiauto.*
import models.binance.{Coin, Network, Saving, Ticker as BinanceTicker}

object Circe {
  given Decoder[List[Network]] = deriveDecoder[List[Network]]
}
