package actors

import actors.NotifyJob.ExecuteTask
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.softwaremill.macwire.wire
import commons.CommonUtil.getFormattedNowDate
import commons.{Configuration, ConfigurationImpl}
import services.{LineService, LineServiceImpl, SatangService, SatangServiceImpl, UserService, UserServiceImpl}

import scala.concurrent.Future

class NotifyJob(actorContext: ActorContext[ExecuteTask]) extends AbstractBehavior[ExecuteTask](actorContext) {
  import context.executionContext

  implicit val system: ActorSystem[Nothing] = actorContext.system
  private lazy val configuration: Configuration = wire[ConfigurationImpl]
  private lazy val lineService: LineService = wire[LineServiceImpl]
  private lazy val satangService: SatangService = wire[SatangServiceImpl]
  private lazy val userService: UserService = wire[UserServiceImpl]

  override def onMessage(msg: ExecuteTask): Behavior[ExecuteTask] = {
    val message = userService.getBalanceMessageForLine(configuration.satangConfig.userId)

    message.flatMap {
      case Some(m) => lineService.notify(m)
      case _ => Future.successful(false)
    }.foreach {
      case false => println(s"${getFormattedNowDate("E dd MMM YYYY HH:mm:ss", isThai = false)} -> There is some problem with cronjob")
      case _ =>
    }

    this
  }
}

object NotifyJob {
  final case class ExecuteTask()

  def apply(): Behavior[ExecuteTask] = Behaviors.setup(context => new NotifyJob(context))
}
