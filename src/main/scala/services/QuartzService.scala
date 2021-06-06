package services

import akka.actor.typed.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension
import services.SchedulerName.SchedulerName

import scala.concurrent.ExecutionContext

trait QuartzService[T] {
  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit
}

class QuartzServiceImpl[T](implicit system: ActorSystem[T], context: ExecutionContext) extends QuartzService[T] {
  val scheduler: QuartzSchedulerTypedExtension = QuartzSchedulerTypedExtension(system)

  def schedule(name: SchedulerName, receiver: ActorRef[T], msg: T): Unit = {
    val startDate = scheduler.scheduleTyped(name.toString, receiver, msg)

    println(s"start date: ${startDate.toString}")
  }
}

object SchedulerName extends Enumeration {
  type SchedulerName = Value

  val Every3hours, Every10Seconds, Custom = Value
}
