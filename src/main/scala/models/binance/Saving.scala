package models.binance

import io.circe.*
import io.circe.generic.semiauto.*

case class Saving(
    rows: List[PositionAmount],
    total: Int
)
object Saving {
  given Encoder[Saving] = deriveEncoder[Saving]
  given Decoder[Saving] = deriveDecoder[Saving]
}

case class PositionAmount(
    asset: String,
    totalAmount: BigDecimal,
    collateralAmount: BigDecimal
)
object PositionAmount {
  given Encoder[PositionAmount] = deriveEncoder[PositionAmount]
  given Decoder[PositionAmount] = deriveDecoder[PositionAmount]
}
