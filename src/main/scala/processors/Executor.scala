package processors

import actors.NotifyJob
import akka.actor.typed.{ActorRef, ActorSystem, Props}
import com.softwaremill.macwire.wire
import services._

import scala.concurrent.ExecutionContext

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(implicit val system: ActorSystem[Nothing], context: ExecutionContext) {
  val quartzService: QuartzService = wire[QuartzService]

  def execute(): Unit = {

    val receiver = system.
    quartzService.schedule(SchedulerName.Every12And18Hours, receiver, NotifyJob.ExecuteTask)
  }
}
