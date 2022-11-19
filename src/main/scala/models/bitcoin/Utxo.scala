package models.bitcoin

import io.circe._
import io.circe.generic.semiauto._

case class Utxo(txid: String, vout: Long, status: Status, value: Long)
object Utxo {
  given Encoder[Utxo] = deriveEncoder[Utxo]
  given Decoder[Utxo] = deriveDecoder[Utxo]
}

case class Status(confirmed: Boolean, blockHeight: Long, blockHash: String, blockTime: String)
object Status {
  given Encoder[Status] =
    Encoder.forProduct4("confirmed", "block_height", "block_hash", "block_time")(b =>
      (b.confirmed, b.blockHeight, b.blockHash, b.blockTime)
    )
  given Decoder[Status] =
    Decoder.forProduct4("confirmed", "block_height", "block_hash", "block_time")(Status.apply)
}
