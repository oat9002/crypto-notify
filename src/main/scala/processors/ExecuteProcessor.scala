package processors

import actors.*
import org.apache.pekko.actor.typed.*
import commons.{Configuration, LocalLogger}
import services.*
import services.scheduler.{QuartzService, SchedulerName}

import scala.concurrent.{ExecutionContext, Future}

trait ExecuteProcessor extends BaseProcessor

class ExecutorProcessorImpl(using
    configuration: Configuration,
    quartzService: QuartzService[Command]
)(using
    system: ActorSystem[Command],
    context: ExecutionContext
) extends ExecuteProcessor
    with LocalLogger {

  def run(): Future[Boolean] = {
    val notifyCron = SchedulerName.Notify
    val healthCheckCron = SchedulerName.HealthCheck

    logger.info(
      s"Cron name: ${notifyCron.toString}, expression: ${configuration.pekkoConfig.quartz.schedules.get(notifyCron.toString).map(_.expression).getOrElse("")}"
    )
    quartzService.schedule(notifyCron, system, NotifyTask)

    if (configuration.mackerelConfig.exists(_.enabled)) {
      logger.info(
        s"Cron name: ${healthCheckCron.toString}, expression: ${configuration.pekkoConfig.quartz.schedules.get(healthCheckCron.toString).map(_.expression).getOrElse("")}"
      )
      quartzService.schedule(healthCheckCron, system, HealthCheckTask)
    }

    Future.successful(true)
  }
}
