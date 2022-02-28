package models.terra

import com.fasterxml.jackson.annotation.JsonProperty

import scala.reflect.ClassTag

case class QueryResult[T: ClassTag](@JsonProperty("query_result") queryResult: T)

case class ExchangeRate(@JsonProperty("exchange_rate") exchangeRate: String,
                        @JsonProperty("aterra_supply") aTerraSupply: String)
