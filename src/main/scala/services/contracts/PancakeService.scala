package services.contracts

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, Constant}
import contracts.pancake.CakePool
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps
import scala.math.BigDecimal.RoundingMode
import scala.util.{Failure, Success, Try}

trait PancakeService {
  def getPancakeStakeBalance(address: String): Future[Option[BigDecimal]]
}

class PancakeServiceImpl(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends PancakeService with LazyLogging {
  private lazy val web3j: Web3j = Web3j.build(new HttpService(Constant.bscRpcUrl))
  private lazy val cakePool: CakePool = CakePool.load(Constant.cakePoolContractAddress, web3j, Credentials.create("0") ,  new DefaultGasProvider())

  def getPancakeStakeBalance(address: String): Future[Option[BigDecimal]] = {
    for {
      userInfo <- cakePool.userInfo(address).sendAsync().asScala
      pricePerFullShare <- cakePool.getPricePerFullShare.sendAsync().asScala
    } yield {
      Try {
        val shares = BigInt(userInfo.component1())
        val userBoostedShare = userInfo.component7()
        val stakedCake = (BigDecimal(shares * BigInt(pricePerFullShare)) / Math.pow(10, 18)) - BigDecimal(userBoostedShare)

        (stakedCake / Math.pow(10,18)).setScale(6, RoundingMode.HALF_UP)
      } match {
        case Success(value) => Some(value)
        case Failure(exception) =>
          logger.error("cannot get package stake balance", exception)
          None
      }
    }
  }
}
