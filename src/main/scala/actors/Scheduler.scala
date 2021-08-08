package actors

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, ConfigurationImpl}
import services.{BscScanService, BscScanServiceImpl, LineService, LineServiceImpl, SatangService, SatangServiceImpl, UserService, UserServiceImpl}


class Scheduler(actorContext: ActorContext[Command]) extends AbstractBehavior[Command](actorContext) with LazyLogging {  import context.executionContext
  import context.executionContext

  implicit val nothingSystem: ActorSystem[Nothing] = actorContext.system
  private lazy val configuration: Configuration = wire[ConfigurationImpl]
  private lazy val lineService: LineService = wire[LineServiceImpl]
  private lazy val satangService: SatangService = wire[SatangServiceImpl]
  private lazy val bscScanService: BscScanService = wire[BscScanServiceImpl]
  private lazy val userService: UserService = wire[UserServiceImpl]

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case NotifyTask =>
      val now = getFormattedNowDate("E dd MMM YYYY HH:mm:ss", isThai = false)
//      val message = userService.getBalanceMessageForLine(configuration.satangConfig.userId, configuration.bscScanConfig.address)

      logger.info(s"NotifyTask run at $now")

//      message.flatMap {
//        case Some(m) => lineService.notify(m)
//        case _ => Future.successful(false)
//      }.foreach {
//        case false => logger.error(s"$now -> There is some problem with cronjob")
//        case _ =>
//      }
      this
    case HealthCheckTask =>
      logger.info("HealthCheckTask run")
      this
    case _ => this
  }
}

object Scheduler {
  def apply(): Behavior[Command] = Behaviors.setup(context => new Scheduler(context))
}
