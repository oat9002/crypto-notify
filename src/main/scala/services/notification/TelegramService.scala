package services.notification

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.Constant.MessageProvider.Telegram
import commons.{Configuration, Constant, HttpClient, LocalLogger}
import models.telegram.TelegramResponse

import java.net.URLEncoder
import scala.concurrent.{ExecutionContext, Future}

trait TelegramService extends NotificationService

class TelegramServiceImpl(using httpClient: HttpClient, configuration: Configuration)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends TelegramService
    with LocalLogger {
  private val charset = "UTF-8"
  override def notify(message: String): Future[Boolean] = {
    val botToken = configuration.telegramConfig.map(_.botToken).getOrElse("")
    val chatId = configuration.telegramConfig.map(_.chatId).getOrElse("")
    val textParam = s"text=${URLEncoder.encode(message, charset)}"
    val chatIdParam = s"chat_id=${URLEncoder.encode(chatId, charset)}"
    val parseModeParam = s"parse_mode=HTML"
    val baseUrl =
      s"${Constant.telegramUrl}/bot$botToken/sendMessage?$textParam&$chatIdParam&$parseModeParam"

    httpClient
      .get[TelegramResponse](baseUrl, Map("Content-Type" -> "application/x-www-form-urlencoded"))
      .map {
        case Right(response) => response.ok
        case Left(ex) =>
          logger.error("cannot send telegram message", ex)
          false
      }
  }

  override def getProvider: Constant.MessageProvider = Telegram
}
