package di

import actors.Command
import akka.actor.typed.ActorSystem
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import helpers.{TerraHelper, TerraHelperImpl}
import processors.{ExecuteProcessor, ExecutorProcessorImpl, HealthCheckProcessor, HealthCheckProcessorImpl, NotifyProcessor, NotifyProcessorImpl}
import services.crypto
import services.crypto.contracts.{PancakeService, PancakeServiceImpl}
import services.crypto.{BinanceService, BinanceServiceImpl, BitcoinService, BitcoinServiceImpl, BscScanService, BscScanServiceImpl, SatangService, SatangServiceImpl, TerraService, TerraServiceImpl}
import services.healthcheck.{MackerelService, MackerelServiceImpl}
import services.notification.{LineService, LineServiceImpl, NotificationService, NotificationServiceImpl, TelegramService, TelegramServiceImpl}
import services.scheduler.{QuartzService, QuartzServiceImpl}
import services.user.{UserService, UserServiceImpl}

import scala.concurrent.ExecutionContext

class DependencySetup(using system: ActorSystem[Nothing], context: ExecutionContext) {
  given configuration: Configuration = ConfigurationImpl()
  given httpclient: HttpClient = HttpClientImpl()
  given terraHelper: TerraHelper = TerraHelperImpl()
  given satangService: SatangService =
    SatangServiceImpl()
  given bscScanService: BscScanService =
    BscScanServiceImpl()
  given binanceService: BinanceService =
    BinanceServiceImpl()
  given terraService: TerraService =
    TerraServiceImpl()
  given pancakeService: PancakeService =
    PancakeServiceImpl()
  given bitcoinService: BitcoinService = crypto.BitcoinServiceImpl()
  given userService: UserService = UserServiceImpl()
  given mackerelService: MackerelService = MackerelServiceImpl()
  given lineService: LineService = LineServiceImpl()
  given telegramService: TelegramService = TelegramServiceImpl()
  given notificationService: NotificationService =
    NotificationServiceImpl()
  lazy val healthCheckProcessor: HealthCheckProcessor = HealthCheckProcessorImpl()
  lazy val notifyProcessor: NotifyProcessor = NotifyProcessorImpl()
}
