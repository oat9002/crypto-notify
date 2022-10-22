import commons.CommonUtil._
import commons.Constant.EncryptionAlgorithm
import models.configuration.AppConfig
import models.terra.QueryResult
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class UtilsSpec extends AnyFunSpec with Matchers {
  describe("CommonUtils") {
    describe("FormatNumberAnyVal") {
      it("should format correctly") {
        val result = 1.format

        result shouldBe "1"
      }
    }

    describe("FormatBigDecimal") {
      it("should format correctly") {
        val result = BigDecimal(1).format

        result shouldBe "1"
      }
    }

    describe("generateHMAC") {
      it("Hmac512 should return correct result") {
        val result = generateHMAC("test", "test", EncryptionAlgorithm.HmacSHA512)

        result shouldBe "9ba1f63365a6caf66e46348f43cdef956015bea997adeb06e69007ee3ff517df10fc5eb860da3d43b82c2a040c931119d2dfc6d08e253742293a868cc2d82015"
      }
      it("Hmac256 should return correct result") {
        val result = generateHMAC("test", "test", EncryptionAlgorithm.HmacSHA256)

        result shouldBe "88cd2108b5347d973cf39cdf9053d7dd42704876d8c9a9bd8e2d168259d3ddf7"
      }
    }

    describe("base64encode") {
      it("should encode correctly") {
        val result = base64Encode("{\"test\":1}")

        result shouldBe "eyJ0ZXN0IjoxfQ=="
      }
    }

    describe("getFormattedNowDate") {
      it("should return something") {
        val result = getFormattedNowDate()

        result should not be ""
      }
    }
  }

//  describe("JsonUtils") {
//    describe("JsonSerialized") {
//      it("should serialize correctly") {
//        val config = AppConfig(5000)
//        val result = config.toJson
//
//        result shouldBe "{\"port\":5000}"
//      }
//
//      describe("JsonDeserialize") {
//        it("should deserialize correctly") {
//          val config = "{\"port\":5000}"
//          val result = config.toObject[AppConfig] match {
//            case Success(value) => value
//            case _              => AppConfig
//          }
//
//          result shouldBe AppConfig(5000)
//        }
//
//        it("should deserialize array of objects correctly") {
//          val configList = "[{\"port\":5000},{\"port\":6000}]"
//          val result = configList.toObject[Array[AppConfig]] match {
//            case Success(value) => value
//            case _              => Array(AppConfig, AppConfig)
//          }
//
//          result.head shouldBe AppConfig(5000)
//          result(1) shouldBe AppConfig(6000)
//        }
//
//        it("should deserialize List of objects correctly") {
//          val configList = "[{\"port\":5000},{\"port\":6000}]"
//          val result = configList.toObject[List[AppConfig]] match {
//            case Success(value) => value
//            case _              => List(AppConfig, AppConfig)
//          }
//
//          result.head shouldBe AppConfig(5000)
//          result(1) shouldBe AppConfig(6000)
//        }
//
//        it("should deserialize generic of objects correctly") {
//          val configList = "{\"query_result\":{\"port\":5000}}"
//          val result = configList.toObject[QueryResult[AppConfig]] match {
//            case Success(value) => value
//            case _              => QueryResult[AppConfig](AppConfig(0))
//          }
//
//          result.queryResult shouldBe AppConfig(5000)
//        }
//      }
//    }
//  }
}
