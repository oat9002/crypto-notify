package di

import org.apache.pekko.actor.typed.ActorSystem
import commons.*
import helpers.{TerraHelper, TerraHelperImpl}
import processors.{
  HealthCheckProcessor,
  HealthCheckProcessorImpl,
  NotifyProcessor,
  NotifyProcessorImpl
}
import services.crypto
import services.crypto.contracts.{PancakeService, PancakeServiceImpl}
import services.crypto.*
import services.healthcheck.{MackerelService, MackerelServiceImpl}
import services.notification.{NotificationService, TelegramServiceImpl}
import services.user.{UserService, UserServiceImpl}

import scala.concurrent.ExecutionContext

class DependencySetup(using system: ActorSystem[Nothing], context: ExecutionContext) {
  given configuration: Configuration = ConfigurationImpl()
  given httpclient: HttpClient = HttpClientImpl()
  given notificationService: NotificationService = TelegramServiceImpl()
  given logger: Logger = Logger()
  given terraHelper: TerraHelper = TerraHelperImpl()
  given satangService: SatangService =
    SatangServiceImpl()
  given etherScanService: EtherScanService =
    EtherScanServiceImpl()
  given binanceService: BinanceService =
    BinanceServiceImpl()
  given terraService: TerraService =
    TerraServiceImpl()
  given pancakeService: PancakeService =
    PancakeServiceImpl()
  given bitcoinService: BitcoinService = crypto.BitcoinServiceImpl()
  given userService: UserService = UserServiceImpl()
  given mackerelService: MackerelService = MackerelServiceImpl()
  lazy val healthCheckProcessor: HealthCheckProcessor = HealthCheckProcessorImpl()
  lazy val notifyProcessor: NotifyProcessor = NotifyProcessorImpl()
}
