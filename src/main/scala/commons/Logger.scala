package commons

import com.typesafe.scalalogging.LazyLogging
import services.notification.TelegramService

class Logger(telegramService: TelegramService) extends LazyLogging {
  def error(message: String, cause: Throwable): Unit = {
    logger.error(message, cause)

    telegramService.notify(s"[Error] - $message")
  }

  def debug(message: String): Unit = {
    logger.debug(message)

    telegramService.notify(s"[Debug] - $message")
  }
}
