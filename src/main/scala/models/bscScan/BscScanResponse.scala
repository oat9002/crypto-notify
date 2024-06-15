package models.bscScan

import io.circe.*
import io.circe.generic.semiauto.*

case class BscScanResponse(status: Int, message: String, result: BigInt)
object BscScanResponse {
  given Encoder[BscScanResponse] = deriveEncoder[BscScanResponse]
  given Decoder[BscScanResponse] = deriveDecoder[BscScanResponse]
}
