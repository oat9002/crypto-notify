package processors

import akka.actor.typed.ActorSystem
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, Logger}
import services.notification.NotificationService
import services.user.UserService

import scala.concurrent.{ExecutionContext, Future}

trait NotifyProcessor extends BaseProcessor

class NotifyProcessorImpl(using
    configuration: Configuration,
    notificationService: NotificationService,
    userService: UserService,
    logger: Logger
)(using system: ActorSystem[Nothing], context: ExecutionContext)
    extends NotifyProcessor {

  override def run(): Future[Boolean] = {
    val now = getFormattedNowDate("E dd MMM YYYY HH:mm:ss", isThai = false)
    val message = userService.getBalanceMessageForLine(
      configuration.satangConfig.userId,
      configuration.bscScanConfig.map(_.address),
      configuration.terraConfig.map(_.address),
      configuration.bitcoinConfig.map(_.address)
    )

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
