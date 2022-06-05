package services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension
import com.typesafe.scalalogging.LazyLogging
import services.SchedulerName.SchedulerName

import scala.concurrent.ExecutionContext

trait QuartzService[T] {
  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit
}

class QuartzServiceImpl[T](implicit
    system: ActorSystem[T],
    context: ExecutionContext
) extends QuartzService[T]
    with LazyLogging {
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
