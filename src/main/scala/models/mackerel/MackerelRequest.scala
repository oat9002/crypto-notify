package models.mackerel

import java.time.Instant
import io.circe._
import io.circe.generic.semiauto._

case class MackerelRequest(name: String, time: Long, value: Int)

object MackerelRequest {
  given Encoder[MackerelRequest] = deriveEncoder[MackerelRequest]
  given Decoder[MackerelRequest] = deriveDecoder[MackerelRequest]
  def apply(name: String, value: Int): MackerelRequest = {
    val unixTimeStamp = Instant.now().getEpochSecond

    MackerelRequest(name, unixTimeStamp, value)
  }
}
