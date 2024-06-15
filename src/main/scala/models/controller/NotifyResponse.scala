package models.controller

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class NotifyResponse(isSuccess: Boolean)

object NotifyResponse {
  given Encoder[NotifyResponse] = deriveEncoder[NotifyResponse]
  given Decoder[NotifyResponse] = deriveDecoder[NotifyResponse]
}
