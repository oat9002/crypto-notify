package models

import com.fasterxml.jackson.annotation.JsonProperty

case class Address(address: String,
                   tag: String,
                   network: String)

case class Wallet(addresses: List[Address],
                  @JsonProperty("available_balance") availableBalance: BigDecimal)

case class GetBalanceResponse(id: Int,
                              email: String,
                              @JsonProperty("identity_verification_level") identityVerificationLevel: String,
                              wallets: Map[String, Wallet])