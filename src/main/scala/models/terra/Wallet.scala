package models.terra

import io.circe._
import io.circe.generic.semiauto._

case class Wallet(balances: List[Balance])
object Wallet {
  given Encoder[Wallet] = deriveEncoder[Wallet]
  given Decoder[Wallet] = deriveDecoder[Wallet]
}

case class Balance(symbol: String, amount: BigDecimal)
object Balance {
  given Encoder[Balance] = deriveEncoder[Balance]
  given Decoder[Balance] = deriveDecoder[Balance]
}

case class RawWallet(balances: List[RawBalance])
object RawWallet {
  given Encoder[RawWallet] = deriveEncoder[RawWallet]
  given Decoder[RawWallet] = deriveDecoder[RawWallet]
}

case class RawBalance(denom: String, amount: Long)
object RawBalance {
  given Encoder[RawBalance] = deriveEncoder[RawBalance]
  given Decoder[RawBalance] = deriveDecoder[RawBalance]
}
