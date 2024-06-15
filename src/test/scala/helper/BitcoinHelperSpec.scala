package helper

import helpers.BitcoinHelper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BitcoinHelperSpec extends AnyWordSpec with Matchers {
  "fromSatoshiToBitcoin" should {
    "convert correctly" in {
      val result = BitcoinHelper.fromSatoshiToBitcoin(800000L)

      result shouldBe BigDecimal(0.008)
    }
  }
}
