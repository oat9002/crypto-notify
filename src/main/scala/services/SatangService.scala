package services

trait SatangService {
  def getBalance(userId: String): String
}

class SatangServiceImpl extends SatangService {
  def getBalance(userId: String): String = ???
}
