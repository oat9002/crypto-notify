package models.line

import io.circe.*
import io.circe.generic.semiauto.*

case class LineResponse(status: Int, message: String)
object LineResponse {
  given Encoder[LineResponse] = deriveEncoder[LineResponse]
  given Decoder[LineResponse] = deriveDecoder[LineResponse]
}
