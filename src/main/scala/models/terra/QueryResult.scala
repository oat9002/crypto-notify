package models.terra

import io.circe.*
import io.circe.generic.semiauto.*

case class QueryResult[T](queryResult: T)
object QueryResult {
  given [T: Encoder]: Encoder[QueryResult[T]] = Encoder.forProduct1("data")(_.queryResult)
  given [T: Decoder]: Decoder[QueryResult[T]] =
    Decoder.forProduct1("data")(QueryResult[T].apply)
}

case class ExchangeRate(
    exchangeRate: BigDecimal,
    aTerraSupply: String
)
object ExchangeRate {
  given Encoder[ExchangeRate] =
    Encoder.forProduct2("exchange_rate", "aterra_supply")(e => (e.exchangeRate, e.aTerraSupply))
  given Decoder[ExchangeRate] =
    Decoder.forProduct2("exchange_rate", "aterra_supply")(ExchangeRate.apply)
}

case class aUstBalance(balance: Long)
object aUstBalance {
  given Encoder[aUstBalance] = deriveEncoder[aUstBalance]
  given Decoder[aUstBalance] = deriveDecoder[aUstBalance]
}
