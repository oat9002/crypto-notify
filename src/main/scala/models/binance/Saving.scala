package models.binance

import io.circe._
import io.circe.generic.semiauto._

case class Saving(
    totalAmountInBtc: BigDecimal,
    totalAmountInUsdt: BigDecimal,
    totalFixedAmountInBtc: BigDecimal,
    totalFixedAmountInUsdt: BigDecimal,
    totalFlexibleInBtc: BigDecimal,
    totalFlexibleInUsdt: BigDecimal,
    positionAmountVos: List[PositionAmount]
)
object Saving {
  given Encoder[Saving] = deriveEncoder[Saving]
  given Decoder[Saving] = deriveDecoder[Saving]
}

case class PositionAmount(
    asset: String,
    amount: BigDecimal,
    amountInBtc: BigDecimal,
    amountInUsdt: BigDecimal
)
object PositionAmount {
  given Encoder[PositionAmount] = deriveEncoder[PositionAmount]
  given Decoder[PositionAmount] = deriveDecoder[PositionAmount]
}
