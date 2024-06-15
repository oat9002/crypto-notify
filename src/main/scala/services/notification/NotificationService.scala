package services.notification

import scala.concurrent.Future

trait NotificationService {
  def notify(message: String): Future[Boolean]
}
