package processors

import actors.NotifyJob
import actors.NotifyJob.ExecuteTask
import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import services._

import scala.concurrent.ExecutionContextExecutor

trait Executor {
  def execute(): Unit
}

class ExecutorImpl {
  implicit val system: ActorSystem[ExecuteTask] = ActorSystem(NotifyJob(), "notify")
  implicit val context: ExecutionContextExecutor = system.executionContext
  val quartzService: QuartzService[ExecuteTask] = wire[QuartzService[ExecuteTask]]

  def execute(): Unit = {
    quartzService.schedule(SchedulerName.Every12And18Hours, system, ExecuteTask())
  }
}
