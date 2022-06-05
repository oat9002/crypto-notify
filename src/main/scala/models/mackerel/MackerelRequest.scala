package models.mackerel

import java.time.Instant

case class MackerelRequest(name: String, time: Long, value: Int)

object MackerelRequest {
  def apply(name: String, value: Int): MackerelRequest = {
    val unixTimeStamp = Instant.now().getEpochSecond

    MackerelRequest(name, unixTimeStamp, value)
  }
}
