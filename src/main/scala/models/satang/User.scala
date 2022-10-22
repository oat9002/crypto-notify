package models.satang

import io.circe._
import io.circe.generic.semiauto._

case class Address(address: String, tag: String, network: String)
object Address {
  given Encoder[Address] = deriveEncoder[Address]
  given Decoder[Address] = deriveDecoder[Address]
}

case class Wallet(
    addresses: Option[List[Address]],
    availableBalance: BigDecimal
)
object Wallet {
  given Encoder[Wallet] =
    Encoder.forProduct2("addresses", "available_balance")(w => (w.addresses, w.availableBalance))
  given Decoder[Wallet] = Decoder.forProduct2("addresses", "available_balance")(Wallet.apply)
}

case class User(
    id: Int,
    email: String,
    identityVerificationLevel: String,
    wallets: Map[String, Wallet]
)
object User {
  given Encoder[User] =
    Encoder.forProduct4("id", "email", "identity_verification_level", "wallets")(u =>
      (u.id, u.email, u.identityVerificationLevel, u.wallets)
    )
  given Decoder[User] =
    Decoder.forProduct4("id", "email", "identity_verification_level", "wallets")(User.apply)
}
