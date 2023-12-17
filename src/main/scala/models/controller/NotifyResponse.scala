package models.controller

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class NotifyResponse(isSuccess: Boolean)

object NotifyResponse {
  given Encoder[NotifyResponse] = deriveEncoder[NotifyResponse]
  given Decoder[NotifyResponse] = deriveDecoder[NotifyResponse]
}
