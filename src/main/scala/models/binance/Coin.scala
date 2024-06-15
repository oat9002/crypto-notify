package models.binance

import io.circe.*
import io.circe.generic.semiauto.*

case class Coin(
    coin: String,
    depositAllEnable: Boolean,
    free: BigDecimal,
    freeze: BigDecimal,
    ipoable: BigDecimal,
    ipoing: BigDecimal,
    isLegalMoney: Boolean,
    locked: BigDecimal,
    name: String,
    networkList: List[Network],
    storage: BigDecimal,
    trading: Boolean,
    withdrawAllEnable: Boolean,
    withdrawing: BigDecimal
)
object Coin {
  given Encoder[Coin] = deriveEncoder[Coin]
  given Decoder[Coin] = deriveDecoder[Coin]
}

case class Network(
    addressRegex: String,
    coin: String,
    depositDesc: String,
    isDefault: Boolean,
    memoRegex: String,
    minConfirm: Int,
    name: String,
    network: String,
    resetAddressStatus: Boolean,
    unLockConfirm: Int,
    withdrawDesc: String,
    withdrawEnable: Boolean,
    withdrawFee: BigDecimal,
    withdrawIntegerMultiple: BigDecimal,
    withdrawMax: BigDecimal,
    withdrawMin: BigDecimal,
    sameAddress: Boolean,
    estimatedArrivalTime: Int,
    busy: Boolean
)
object Network {
  given Encoder[Network] = deriveEncoder[Network]
  given Decoder[Network] = deriveDecoder[Network]
}
