package processors

import actors.{Command, HealthCheckTask, NotifyTask, Scheduler}
import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(configuration: Configuration)(implicit val system: ActorSystem[Command], context: ExecutionContext) extends Executor with LazyLogging {
  private lazy val quartzService: QuartzService[Command] = wire[QuartzServiceImpl[Command]]

  def execute(): Unit = {
    val notifyCron = SchedulerName.Notify
    val healthCheckCron = SchedulerName.HealthCheck

    logger.info(s"Cron name: ${notifyCron.toString}, expression: ${configuration.akkaConfig.quartz.schedules.get(notifyCron.toString).map(_.expression).getOrElse("")}")
    quartzService.schedule(notifyCron, system, NotifyTask)
    logger.info(s"Cron name: ${healthCheckCron.toString}, expression: ${configuration.akkaConfig.quartz.schedules.get(healthCheckCron.toString).map(_.expression).getOrElse("")}")
    quartzService.schedule(healthCheckCron, system, HealthCheckTask)
  }
}
