package models.line

import io.circe._
import io.circe.generic.semiauto._

case class LineResponse(status: Int, message: String)
object LineResponse {
  given Encoder[LineResponse] = deriveEncoder[LineResponse]
  given Decoder[LineResponse] = deriveDecoder[LineResponse]
}
