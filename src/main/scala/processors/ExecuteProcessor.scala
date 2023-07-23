package processors

import actors.*
import akka.actor.typed.*
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import models.configuration.Mode
import services.*
import services.scheduler.{QuartzService, QuartzServiceImpl, SchedulerName}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait ExecuteProcessor extends BaseProcessor

class ExecutorProcessorImpl(using configuration: Configuration,  quartzService: QuartzService[Command])(using
    system: ActorSystem[Command],
    context: ExecutionContext
) extends ExecuteProcessor
    with LazyLogging {

  def run(): Future[Boolean] = {
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
    
    Future.successful(true)
  }
}
