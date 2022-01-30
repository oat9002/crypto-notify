package commons

object Constant {
  val CakeTokenContractAddress = "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82"
  val CakeTokenStakeContractAddress = "0x009cf7bc57584b7998236eff51b98a168dcea9b0"
  val BetaTokenContractAddress = "0xBe1a001FE942f96Eea22bA08783140B9Dcc09D28"
}

object HmacAlgorithm extends Enumeration {
  type HmacAlgorithm = Value
  val HmacSHA512, HmacSHA256 = Value
}
