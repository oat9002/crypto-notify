package models.mackerel

import io.circe.*
import io.circe.generic.semiauto.*

case class MackerelResponse(success: Boolean)
object MackerelResponse {
  given Encoder[MackerelResponse] = deriveEncoder[MackerelResponse]
  given Decoder[MackerelResponse] = deriveDecoder[MackerelResponse]
}
