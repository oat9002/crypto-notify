package services.notification

import commons.Constant.MessageProvider

import scala.concurrent.Future

trait NotificationService {
  def getProvider: MessageProvider
  def notify(message: String): Future[Boolean]
}
