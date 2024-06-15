package models.mackerel

import io.circe.*
import io.circe.generic.semiauto.*

import java.time.Instant

case class MackerelRequest(name: String, time: Long, value: Int)

object MackerelRequest {
  given Encoder[MackerelRequest] = deriveEncoder[MackerelRequest]
  given Decoder[MackerelRequest] = deriveDecoder[MackerelRequest]
  def apply(name: String, value: Int): MackerelRequest = {
    val unixTimeStamp = Instant.now().getEpochSecond

    MackerelRequest(name, unixTimeStamp, value)
  }
}
