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
    configuration: Configuration,
    notificationService: NotificationService,
    userService: UserService
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends NotifyProcessor
    with LazyLogging {

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
