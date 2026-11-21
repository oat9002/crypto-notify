package models.binance

import io.circe.parser.decode
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CoinSpec extends AnyWordSpec with Matchers {
  "The Binance account detail decoder" should {
    "keep balances when a coin has no name" in {
      val response = """[
        {
          "coin": "BTC",
          "depositAllEnable": true,
          "free": "0.25",
          "freeze": "0",
          "ipoable": "0",
          "ipoing": "0",
          "isLegalMoney": false,
          "locked": "0",
          "name": "Bitcoin",
          "networkList": [],
          "storage": "0",
          "trading": true,
          "withdrawAllEnable": true,
          "withdrawing": "0"
        },
        {
          "coin": "ETH",
          "depositAllEnable": true,
          "free": "1.5",
          "freeze": "0",
          "ipoable": "0",
          "ipoing": "0",
          "isLegalMoney": false,
          "locked": "0",
          "networkList": [],
          "storage": "0",
          "trading": true,
          "withdrawAllEnable": true,
          "withdrawing": "0"
        }
      ]"""

      decode[List[Coin]](response).map(_.map(coin => (coin.coin, coin.free, coin.name))) shouldBe
        Right(List(("BTC", BigDecimal("0.25"), Some("Bitcoin")), ("ETH", BigDecimal("1.5"), None)))
    }
  }
}
