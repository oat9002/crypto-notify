package helpers

object BitcoinHelper {
  def fromSatoshiToBitcoin(satoshi: Long): BigDecimal = BigDecimal(satoshi / Math.pow(10, 8))
}
