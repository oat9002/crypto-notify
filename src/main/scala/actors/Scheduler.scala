package actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import commons.LocalLogger
import processors.{HealthCheckProcessor, NotifyProcessor}

class Scheduler(using notifyProcessor: NotifyProcessor, healthCheckProcessor: HealthCheckProcessor)(
    using actorContext: ActorContext[Command]
) extends AbstractBehavior[Command](actorContext)
    with LocalLogger {

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
  def apply(
      notifyProcessor: NotifyProcessor,
      healthCheckProcessor: HealthCheckProcessor
  ): Behavior[Command] =
    Behaviors.setup(context => {
      given ActorContext[Command] = context
      given NotifyProcessor = notifyProcessor
      given HealthCheckProcessor = healthCheckProcessor

      new Scheduler()
    })
}
