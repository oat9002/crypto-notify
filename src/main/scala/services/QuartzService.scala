package services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension
import services.SchedulerName.SchedulerName

import scala.concurrent.ExecutionContext

class QuartzService(implicit system: ActorSystem[Nothing], context: ExecutionContext) {
  val scheduler: QuartzSchedulerTypedExtension = QuartzSchedulerTypedExtension(system)

  def schedule[T](name: SchedulerName, receiver: ActorRef[T], msg: T): Unit = {
    val startDate = scheduler.scheduleTyped(name.toString, receiver, msg)

    println(s"start date: ${startDate.toString}")
  }
}

object SchedulerName extends Enumeration {
  type SchedulerName = Value

  val Every12And18Hours = Value
}
