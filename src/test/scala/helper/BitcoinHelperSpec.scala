package helper

import helpers.BitcoinHelper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class BitcoinHelperSpec extends AnyWordSpec with Matchers {
  "fromSatoshiToBitcoin" should {
    "convert correctly" in {
      val result = BitcoinHelper.fromSatoshiToBitcoin(800000L)

      result shouldBe BigDecimal(0.008)
    }
  }
}
