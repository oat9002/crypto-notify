package processors

import actors.NotifyJob
import actors.NotifyJob.ExecuteTask
import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import services._

import scala.concurrent.ExecutionContextExecutor

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(val configuration: Configuration) extends Executor with LazyLogging {
  implicit val system: ActorSystem[ExecuteTask] = ActorSystem(NotifyJob(), "notify")
  implicit val context: ExecutionContextExecutor = system.executionContext
  lazy val quartzService: QuartzService[ExecuteTask] = wire[QuartzServiceImpl[ExecuteTask]]

  def execute(): Unit = {
    val selectedScheduler = SchedulerName.Custom

    logger.info(s"Cron name: ${selectedScheduler.toString}, expression: ${configuration.akkaConfig.quartz.schedules.get(selectedScheduler.toString).map(_.expression).getOrElse("")}")
    quartzService.schedule(selectedScheduler, system, ExecuteTask())
  }
}
