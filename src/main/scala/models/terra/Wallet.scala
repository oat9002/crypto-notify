package models.terra

case class Wallet(balances: List[Balance])

case class Balance(symbol: String, amount: BigDecimal)

case class RawWallet(balances: Array[RawBalance])

case class RawBalance(denom: String, amount: Long)
