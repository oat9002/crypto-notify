package services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension
import services.SchedulerName.SchedulerName

import scala.concurrent.ExecutionContext

class QuartzService[T](implicit system: ActorSystem[T], context: ExecutionContext) {
  val scheduler: QuartzSchedulerTypedExtension = QuartzSchedulerTypedExtension(system)

  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit = {
    val startDate = scheduler.scheduleTyped(name.toString, receiver, msg)

    println(s"start date: ${startDate.toString}")
  }
}

object SchedulerName extends Enumeration {
  type SchedulerName = Value

  val Every12And18Hours = Value
}
