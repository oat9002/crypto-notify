package models

import com.fasterxml.jackson.annotation.JsonProperty

case class Wallet(addresses: Array[String],
                  @JsonProperty("available_balance") availableBalance: BigDecimal)

case class GetBalanceResponse(id: Int,
                              email: String,
                              @JsonProperty("identity_verificationLevel") identityVerificationLevel: String,
                              wallet: Map[String, Wallet])