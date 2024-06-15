package services.crypto.contracts

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.{Configuration, Constant, Logger}
import contracts.pancake.{CakePool, VeCakePool}
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

class PancakeServiceImpl(using
    system: ActorSystem[Nothing],
    context: ExecutionContext,
                         logger: Logger
) extends PancakeService {
  private val gasProvider = new DefaultGasProvider()
  private val defaultCredential = Credentials.create("0")
  private lazy val web3j: Web3j =
    Web3j.build(new HttpService(Constant.bscRpcUrl))
  private lazy val cakePool: CakePool = CakePool.load(
    Constant.cakePoolContractAddress,
    web3j,
    defaultCredential,
    gasProvider
  )
  private lazy val veCakePool: VeCakePool = VeCakePool.load(
    Constant.veCakePoolContractAddress,
    web3j,
    defaultCredential,
    gasProvider
  )

  def getPancakeStakeBalance(address: String): Future[Option[BigDecimal]] = {
    val oldCakeF = getCakeBalanceFromOldPool(address)
    val cakeFromVePoolF = getCakeBalanceFromVeCakePool(address)

    for {
      oldCakeOpt <- oldCakeF
      cakeFromVeOpt <- cakeFromVePoolF
    } yield {
      if (oldCakeOpt.isEmpty && cakeFromVeOpt.isEmpty) {
        None
      } else {
        val oldCake = oldCakeOpt.getOrElse(BigDecimal(0))
        val cakeFromVe = cakeFromVeOpt.getOrElse(BigDecimal(0))

        Some(oldCake + cakeFromVe)
      }
    }
  }

  private def getCakeBalanceFromOldPool(address: String): Future[Option[BigDecimal]] = {
    for {
      userInfo <- cakePool.userInfo(address).sendAsync().asScala
      pricePerFullShare <- cakePool.getPricePerFullShare.sendAsync().asScala
    } yield {
      Try {
        val shares = BigInt(userInfo.component1())
        val userBoostedShare = userInfo.component7()
        val stakedCake = (BigDecimal(shares * BigInt(pricePerFullShare)) / Math
          .pow(10, 18)) - BigDecimal(userBoostedShare)

        (stakedCake / Math.pow(10, 18)).setScale(6, RoundingMode.HALF_UP)
      } match {
        case Success(value) => Some(value)
        case Failure(exception) =>
          logger.error("cannot get pancake stake balance", exception)
          None
      }
    }
  }

  private def getCakeBalanceFromVeCakePool(address: String): Future[Option[BigDecimal]] = {
    for {
      userInfo <- veCakePool.getUserInfo(address).sendAsync().asScala
    } yield {
      Try {
        val balance = BigDecimal(userInfo.component1())

        (balance / Math.pow(10, 18)).setScale(6, RoundingMode.HALF_UP)
      } match {
        case Success(value) => Some(value)
        case Failure(exception) =>
          logger.equals("cannot get cake from veCakePool", exception)
          None
      }
    }
  }
}
