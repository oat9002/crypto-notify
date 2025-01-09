package commons

object Constant {
  val CakeTokenContractAddress = "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"
  val BetaTokenContractAddress = "0xBe1a001FE942f96Eea22bA08783140B9Dcc09D28"
  val cakePoolContractAddress = "0x45c54210128a065de780C4B0Df3d16664f7f859e"
  val veCakePoolContractAddress = "0x5692DB8177a81A6c6afc8084C2976C9933EC1bAB"

  val bscRpcUrl = "https://bsc-dataseed.binance.org"
  val satangUrl = "https://www.orbixtrade.com/api/"
  val bscScanUrl = "https://api.bscscan.com/api"
  val binanceUrl = "https://api.binance.com"
  val terraUrl = "https://terra-classic-lcd.publicnode.com"
  val twoPointOTerraUrl = "https://phoenix-lcd.terra.dev"
  val makerelUrl = "https://api.mackerelio.com"
  val lineNotifyUrl = "https://notify-api.line.me/api/notify"
  val telegramUrl = "https://api.telegram.org"
  val blockStreamUrl = "https://blockstream.info/api"

  enum EncryptionAlgorithm:
    case HmacSHA512, HmacSHA256

  enum MessageProvider:
    case Line, Telegram
}
