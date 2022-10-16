package commons

import io.circe.*
import io.circe.generic.semiauto.*
import models.binance.{Coin, Network, PositionAmount, Saving, Ticker as BinanceTicker}

object Circe {
  given networkListDecoder: Decoder[List[Network]] = deriveDecoder[List[Network]]
  given cointListDecoder: Decoder[List[Coin]] = deriveDecoder[List[Coin]]
  given positionAmountListDecoder: Decoder[List[PositionAmount]] =
    deriveDecoder[List[PositionAmount]]
  given savingDecoder: Decoder[Saving] = deriveDecoder[Saving]
  given coinDecoder: Decoder[BinanceTicker] = deriveDecoder[BinanceTicker]
}
