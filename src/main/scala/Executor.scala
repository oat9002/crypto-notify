import akka.actor.ActorSystem
import com.softwaremill.macwire.wire
import services.{JobRunrService, JobRunrServiceImpl, SatangService, SatangServiceImpl}

import scala.concurrent.ExecutionContext

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(implicit val actor: ActorSystem, context: ExecutionContext) extends Executor {
  private lazy val satangService: SatangService = wire[SatangServiceImpl]
  private lazy val jobRunrService: JobRunrService = wire[JobRunrServiceImpl]

  def execute(): Unit = {

  }
}
