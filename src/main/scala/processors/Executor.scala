package processors

import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import services._

import scala.concurrent.ExecutionContext

trait Executor {
  def execute(): Unit
}

class ExecutorImpl(implicit val actor: ActorSystem[Nothing], context: ExecutionContext) extends Executor {
  lazy val configuraiton: Configuration = wire[ConfigurationImpl]
  lazy val userService: UserService = wire[UserServiceImpl]
  lazy val lineService: LineService = wire[LineServiceImpl]
  lazy val jobRunrService: JobRunrService = wire[JobRunrServiceImpl]

  def execute(): Unit = {
    jobRunrService.enqueue(() => {
      val message = userService.getBalanceMessageForLine(configuraiton.satangConfig.userId)

      message.foreach {
        case Some(m) => lineService.notify(m)
        case _ =>
      }
    })
//    jobRunrService.recuring(Cron.minutely())(() => {
//      val message = userService.getBalanceMessageForLine(configuraiton.satangConfig.userId)
//
//      message.foreach {
//        case Some(m) => lineService.notify(m)
//        case _ =>
//      }
//    })
  }
}
