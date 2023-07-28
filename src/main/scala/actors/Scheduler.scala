package actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.typesafe.scalalogging.LazyLogging
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, ConfigurationImpl, HttpClient, HttpClientImpl}
import helpers.{TerraHelper, TerraHelperImpl}
import models.mackerel.MackerelRequest
import processors.{
  HealthCheckProcessor,
  HealthCheckProcessorImpl,
  NotifyProcessor,
  NotifyProcessorImpl
}
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

import scala.concurrent.Future

class Scheduler(using notifyProcessor: NotifyProcessor, healthCheckProcessor: HealthCheckProcessor)(using actorContext: ActorContext[Command])
    extends AbstractBehavior[Command](actorContext)
    with LazyLogging {
  import context.executionContext

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case NotifyTask =>
      notifyProcessor.run()

      this
    case HealthCheckTask =>
      healthCheckProcessor.run()
      this
    case _ => this
  }
}

object Scheduler {
  def apply(using notifyProcessor: NotifyProcessor, healthCheckProcessor: HealthCheckProcessor): Behavior[Command] =
    Behaviors.setup(context => new Scheduler(context))
}
