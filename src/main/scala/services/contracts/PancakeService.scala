package services.contracts

import akka.actor.typed.ActorSystem
import commons.{Configuration, Constant}
import contracts.pancake.CakePool
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.RoundingMode

trait PanCakeService {
  def getPancakeStakeBalance(address: String): BigDecimal
}

class PancakeServiceImpl(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends PanCakeService {
  val web3j: Web3j = Web3j.build(new HttpService(Constant.bscRpcUrl))
  val cakePool: CakePool = CakePool.load(Constant.cakePoolContractAddress, web3j, Credentials.create("") ,  new DefaultGasProvider())

  def getPancakeStakeBalance(address: String): BigDecimal = {
    val (shares, _, _, _, _, _, userBoostedShare, _, _) = cakePool.userInfo(address).send().asInstanceOf[Tuple9[BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, BigInt, Boolean, BigInt]]
    val pricePerFullShare = cakePool.getPricePerFullShare.send().asInstanceOf[BigInt]
    val stakeCaked = (BigDecimal(shares * pricePerFullShare) / 10 pow 18) - BigDecimal(userBoostedShare)

    stakeCaked.setScale(6, RoundingMode.HALF_UP)
  }

}
