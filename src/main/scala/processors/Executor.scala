package processors

import actors.*
import akka.actor.typed.*
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import models.configuration.Mode
import services.*
import services.scheduler.{QuartzService, QuartzServiceImpl, SchedulerName}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(configuration: Configuration)(using
    val system: ActorSystem[Command],
    context: ExecutionContext
) extends Executor
    with LazyLogging {
  private lazy val quartzService: QuartzService[Command] = QuartzServiceImpl[Command]()

  def execute(): Unit = {
    val notifyCron = SchedulerName.Every10Seconds
    val healthCheckCron = SchedulerName.HealthCheck

    logger.info(
      s"Cron name: ${notifyCron.toString}, expression: ${configuration.akkaConfig.quartz.schedules.get(notifyCron.toString).map(_.expression).getOrElse("")}"
    )
    quartzService.schedule(notifyCron, system, NotifyTask)

    if (configuration.mackerelConfig.exists(_.enabled)) {
      logger.info(
        s"Cron name: ${healthCheckCron.toString}, expression: ${configuration.akkaConfig.quartz.schedules.get(healthCheckCron.toString).map(_.expression).getOrElse("")}"
      )
      quartzService.schedule(healthCheckCron, system, HealthCheckTask)
    }
  }
}
