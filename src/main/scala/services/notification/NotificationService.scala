package services.notification

import commons.Configuration

import scala.concurrent.Future

trait NotificationService {
  def notify(message: String): Future[Boolean]
}

class NotificationServiceImpl(config: Configuration, lineService: LineService, telegramService: TelegramService) extends NotificationService {
  override def notify(message: String): Future[Boolean] = {
    if (config.telegramConfig.exists(t => t.chatId.nonEmpty && t.botToken.nonEmpty)) {
      telegramService.notify(message)
    } else {
      lineService.notify(message)
    }
  }
}
