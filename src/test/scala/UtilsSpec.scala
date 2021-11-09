import commons.JsonUtil.{JsonDeserialize, JsonSerialized}
import commons.CommonUtil._
import models.configuration.AppConfig
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class UtilsSpec extends AnyFunSpec with Matchers {
  describe("CommonUtils") {
    it("format int") {
      val result = 1.format

      result shouldBe "1"
    }
  }

  describe("JsonUtils") {
    it("serialize to string") {
      val config = AppConfig(5000)
      val result = config.toJson

      result shouldBe "{\"port\":5000}"
    }

    it("deserialize to object") {
      val config = "{\"port\":5000}"
      val result = config.toObject[AppConfig]

      result shouldBe AppConfig(5000)
    }
  }
}
