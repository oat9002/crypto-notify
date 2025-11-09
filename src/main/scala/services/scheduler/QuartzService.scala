package services.scheduler

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension
import com.typesafe.scalalogging.LazyLogging
import commons.LocalLogger
import services.scheduler.QuartzService
import services.scheduler.SchedulerName.SchedulerName

import scala.concurrent.ExecutionContext

trait QuartzService[T] {
  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit
}

class QuartzServiceImpl[T](using
    system: ActorSystem[T],
    context: ExecutionContext
) extends QuartzService[T]
    with LocalLogger {
  val scheduler: QuartzSchedulerTypedExtension = QuartzSchedulerTypedExtension(
    system
  )

  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit = {
    val startDate = scheduler.scheduleTyped(name.toString, receiver, msg)

    logger.info(s"start date: ${startDate.toString}")
  }
}

object SchedulerName extends Enumeration {
  type SchedulerName = Value

  val Every3hours, Every10Seconds, Every1Minute, Notify, HealthCheck = Value
}
