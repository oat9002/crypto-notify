package models.terra

import com.fasterxml.jackson.annotation.JsonProperty


case class QueryResult[T](@JsonProperty("query_result") queryResult: T)

case class ExchangeRate(@JsonProperty("exchange_rate") exchangeRate: BigDecimal,
                        @JsonProperty("aterra_supply") aTerraSupply: String)

case class aUstBalance(balance: Long)
