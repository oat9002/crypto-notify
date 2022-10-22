package models.mackerel

import io.circe._
import io.circe.generic.semiauto._

case class MackerelResponse(name: String, time: Long, value: Int)
object MackerelResponse {
  given Encoder[MackerelResponse] = deriveEncoder[MackerelResponse]
  given Decoder[MackerelResponse] = deriveDecoder[MackerelResponse]
}
