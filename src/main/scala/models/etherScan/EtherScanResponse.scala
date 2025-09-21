package models.etherScan

import io.circe.*
import io.circe.generic.semiauto.*

case class EtherScanResponse(status: Int, message: String, result: String)
object EtherScanResponse {
  given Encoder[EtherScanResponse] = deriveEncoder[EtherScanResponse]
  given Decoder[EtherScanResponse] = deriveDecoder[EtherScanResponse]
}
