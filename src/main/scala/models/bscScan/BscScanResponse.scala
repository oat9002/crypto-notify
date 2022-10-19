package models.bscScan

import io.circe._
import io.circe.generic.semiauto._

case class BscScanResponse(status: Int, message: String, result: BigInt)
object BscScanResponse {
  given Encoder[BscScanResponse] = deriveEncoder[BscScanResponse]
  given Decoder[BscScanResponse] = deriveDecoder[BscScanResponse]
}
