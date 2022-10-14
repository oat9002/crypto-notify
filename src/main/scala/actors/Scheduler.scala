package actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import helpers.{TerraHelper, TerraHelperImpl}
import models.mackerel.MackerelRequest
import services.contracts.{PancakeService, PancakeServiceImpl}
import services.{
  BinanceService,
  BinanceServiceImpl,
  BscScanService,
  BscScanServiceImpl,
  LineService,
  LineServiceImpl,
  MackerelService,
  MackerelServiceImpl,
  SatangService,
  SatangServiceImpl,
  TerraService,
  TerraServiceImpl,
  UserService,
  UserServiceImpl
}

import scala.concurrent.Future

class Scheduler(actorContext: ActorContext[Command])
    extends AbstractBehavior[Command](actorContext)
    with LazyLogging {
  import context.executionContext

  given nothingSystem: ActorSystem[Nothing] = actorContext.system
  private lazy val configuration: Configuration = ConfigurationImpl()
  private lazy val httpclient: HttpClient = HttpClientImpl()
  private lazy val terraHelper: TerraHelper = TerraHelperImpl()
  private lazy val lineService: LineService =
    LineServiceImpl(httpclient, configuration)
  private lazy val satangService: SatangService =
    SatangServiceImpl(configuration, httpclient)
  private lazy val bscScanService: BscScanService =
    BscScanServiceImpl(configuration, httpclient)
  private lazy val binanceService: BinanceService =
    BinanceServiceImpl(configuration, httpclient)
  private lazy val terraService: TerraService =
    TerraServiceImpl(configuration, httpclient, terraHelper)
  private lazy val pancakeService: PancakeService =
    PancakeServiceImpl()
  private lazy val userService: UserService = UserServiceImpl(
    satangService,
    bscScanService,
    binanceService,
    terraService,
    pancakeService
  )
  private lazy val mackerelService: MackerelService =
    MackerelServiceImpl(configuration, httpclient)

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case NotifyTask =>
      val now = getFormattedNowDate("E dd MMM YYYY HH:mm:ss", isThai = false)
      val message = userService.getBalanceMessageForLine(
        configuration.satangConfig.userId,
        configuration.bscScanConfig.map(_.address),
        configuration.terraConfig.map(_.address)
      )

      logger.info(s"NotifyTask run at $now")

      message
        .flatMap {
          case Some(m) => lineService.notify(m)
          case _       => Future.successful(false)
        }
        .foreach {
          case false =>
            logger.error(s"$now -> There is some problem with cronjob")
          case _ =>
        }
      this
    case HealthCheckTask =>
      mackerelService.sendMeasurement(List(MackerelRequest("healthCheck", 1)))
      this
    case _ => this
  }
}

object Scheduler {
  def apply(): Behavior[Command] =
    Behaviors.setup(context => new Scheduler(context))
}
