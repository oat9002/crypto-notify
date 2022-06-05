package models.binance

case class Saving(
    totalAmountInBtc: BigDecimal,
    totalAmountInUsdt: BigDecimal,
    totalFixedAmountInBtc: BigDecimal,
    totalFixedAmountInUsdt: BigDecimal,
    totalFlexibleInBtc: BigDecimal,
    totalFlexibleInUsdt: BigDecimal,
    positionAmountVos: Array[PositionAmount]
)

case class PositionAmount(
    asset: String,
    amount: BigDecimal,
    amountInBtc: BigDecimal,
    amountInUsdt: BigDecimal
)
