package commons

import com.typesafe.scalalogging.LazyLogging
import services.notification.NotificationService

class Logger(using notificationService: NotificationService) extends LazyLogging {
  def error(message: String, cause: Throwable): Unit = {
    logger.error(message, cause)

    notificationService.notify(s"[Error] - $message")
  }

  def error(message: String): Unit = {
    logger.error(message)

    notificationService.notify(s"[Error] - $message")
  }

  def debug(message: String): Unit = {
    logger.debug(message)

    notificationService.notify(s"[Debug] - $message")
  }

  def info(message: String): Unit = {
    logger.info(message)

    notificationService.notify(s"[Info] - $message")
  }

  def warn(message: String): Unit = {
    logger.warn(message)

    notificationService.notify(s"[Warn] - $message")
  }
}

trait LocalLogger extends LazyLogging
