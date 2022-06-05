package models.satang

import com.fasterxml.jackson.annotation.JsonProperty

case class Address(address: String, tag: String, network: String)

case class Wallet(
    addresses: Array[Address],
    @JsonProperty("available_balance") availableBalance: BigDecimal
)

case class User(
    id: Int,
    email: String,
    @JsonProperty(
      "identity_verification_level"
    ) identityVerificationLevel: String,
    wallets: Map[String, Wallet]
)
