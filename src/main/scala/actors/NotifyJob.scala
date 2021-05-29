package actors

import actors.NotifyJob.ExecuteTask
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import services.{LineService, LineServiceImpl, UserService, UserServiceImpl}

class NotifyJob(actorContext: ActorContext[ExecuteTask]) extends AbstractBehavior[ExecuteTask](actorContext) {
  import context.executionContext

  implicit val system: ActorSystem[Nothing] = actorContext.system
  private lazy val configuration: Configuration = wire[ConfigurationImpl]
  private lazy val userService: UserService = wire[UserServiceImpl]
  private lazy val lineService: LineService = wire[LineServiceImpl]

  override def onMessage(msg: ExecuteTask): Behavior[ExecuteTask] = {
    val message = userService.getBalanceMessageForLine(configuration.satangConfig.userId)

    message.foreach {
      case Some(m) => lineService.notify(m)
      case _ =>
    }

    this
  }
}

object NotifyJob {
  final case class ExecuteTask()

  def apply(): Behavior[ExecuteTask] = Behaviors.setup(context => new NotifyJob(context))
}
