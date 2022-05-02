package commons

object Constant {
  val CakeTokenContractAddress = "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"
  val BetaTokenContractAddress = "0xBe1a001FE942f96Eea22bA08783140B9Dcc09D28"
  val aUstContractAddress = "terra1hzh9vpxhsk8253se0vv5jj6etdvxu3nv8z07zu"
  val anchorMarketContractAddress = "terra1sepfj7s0aeg5967uxnfk4thzlerrsktkpelm5s"
  val cakePoolContractAddress = "0x45c54210128a065de780C4B0Df3d16664f7f859e"

  val bscRpcUrl = "https://bsc-dataseed.binance.org"
}

object HmacAlgorithm extends Enumeration {
  type HmacAlgorithm = Value
  val HmacSHA512, HmacSHA256 = Value
}
