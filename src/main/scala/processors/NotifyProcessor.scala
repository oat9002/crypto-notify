package processors

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import helpers.{TerraHelper, TerraHelperImpl}
import services.crypto.contracts.{PancakeService, PancakeServiceImpl}
import services.crypto.{
  BinanceService,
  BinanceServiceImpl,
  BitcoinService,
  BitcoinServiceImpl,
  BscScanService,
  BscScanServiceImpl,
  SatangService,
  SatangServiceImpl,
  TerraService,
  TerraServiceImpl
}
import services.healthcheck.{MackerelService, MackerelServiceImpl}
import services.notification.{
  LineService,
  LineServiceImpl,
  NotificationService,
  NotificationServiceImpl,
  TelegramService,
  TelegramServiceImpl
}
import services.user.{UserService, UserServiceImpl}

import scala.concurrent.{ExecutionContext, Future}

trait NotifyProcessor extends BaseProcessor

class NotifyProcessorImpl(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends NotifyProcessor
    with LazyLogging {

  private lazy val configuration: Configuration = ConfigurationImpl()
  private lazy val httpclient: HttpClient = HttpClientImpl()
  private lazy val terraHelper: TerraHelper = TerraHelperImpl()
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
  private lazy val bitcoinService: BitcoinService = BitcoinServiceImpl(configuration, httpclient)
  private lazy val userService: UserService = UserServiceImpl(
    satangService,
    bscScanService,
    binanceService,
    terraService,
    pancakeService,
    bitcoinService
  )
  private lazy val mackerelService: MackerelService = MackerelServiceImpl(configuration, httpclient)
  private lazy val lineService: LineService = LineServiceImpl(httpclient, configuration)
  private lazy val telegramService: TelegramService = TelegramServiceImpl(httpclient, configuration)
  private lazy val notificationService: NotificationService =
    NotificationServiceImpl(configuration, lineService, telegramService)

  override def run(): Future[Boolean] = {
    val now = getFormattedNowDate("E dd MMM YYYY HH:mm:ss", isThai = false)
    val message = userService.getBalanceMessageForLine(
      configuration.satangConfig.userId,
      configuration.bscScanConfig.map(_.address),
      configuration.terraConfig.map(_.address),
      configuration.bitcoinConfig.map(_.address)
    )

    logger.info(s"NotifyTask run at $now")

    message
      .flatMap {
        case Some(m) => notificationService.notify(m)
        case _ =>
          logger.error(s"$now -> There is some problem about getting message")
          Future.successful(false)
      }
      .recover { case ex: Throwable =>
        logger.error(s"$now -> There is some problem with cronjob", ex)
        false
      }
  }
}
