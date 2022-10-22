package models.binance

import io.circe._
import io.circe.generic.semiauto._

case class Saving(
    totalAmountInBTC: BigDecimal,
    totalAmountInUSDT: BigDecimal,
    totalFixedAmountInBTC: BigDecimal,
    totalFixedAmountInUSDT: BigDecimal,
    totalFlexibleInBTC: BigDecimal,
    totalFlexibleInUSDT: BigDecimal,
    positionAmountVos: List[PositionAmount]
)
object Saving {
  given Encoder[Saving] = deriveEncoder[Saving]
  given Decoder[Saving] = deriveDecoder[Saving]
}

case class PositionAmount(
    asset: String,
    amount: BigDecimal,
    amountInBTC: BigDecimal,
    amountInUSDT: BigDecimal
)
object PositionAmount {
  given Encoder[PositionAmount] = deriveEncoder[PositionAmount]
  given Decoder[PositionAmount] = deriveDecoder[PositionAmount]
}
