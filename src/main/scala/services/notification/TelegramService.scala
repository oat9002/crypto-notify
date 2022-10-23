package services.notification

import scala.concurrent.Future

trait TelegramService extends NotificationService

class TelegramServiceImpl extends TelegramService {
  override def notify(message: String): Future[Boolean] = {
    Future.successful(true)
  }
}
