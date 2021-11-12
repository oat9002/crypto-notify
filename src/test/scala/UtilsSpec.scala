import commons.JsonUtil.{JsonDeserialize, JsonSerialize}
import commons.CommonUtil._
import models.configuration.AppConfig
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

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

    describe("generateHMAC512") {
      it("should return correct key") {
        val result = generateHMAC512("test", "test")

        result shouldBe "9ba1f63365a6caf66e46348f43cdef956015bea997adeb06e69007ee3ff517df10fc5eb860da3d43b82c2a040c931119d2dfc6d08e253742293a868cc2d82015"
      }
    }

    describe("getFormattedNowDate") {
      it("should return something") {
        val result = getFormattedNowDate()

        result should not be ""
      }
    }
  }

  describe("JsonUtils") {
    describe("JsonSerialized") {
      it("should serialize correctly") {
        val config = AppConfig(5000)
        val result = config.toJson

        result shouldBe "{\"port\":5000}"
      }

      describe("JsonDeserialize") {
        it("should deserialize correctly") {
          val config = "{\"port\":5000}"
          val result = config.toObject[AppConfig]

          result shouldBe AppConfig(5000)
        }
      }
    }
  }
}
