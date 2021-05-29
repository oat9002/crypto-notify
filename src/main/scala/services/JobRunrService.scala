package services

import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import org.jobrunr.configuration.JobRunr
import org.jobrunr.jobs.lambdas.JobLambda
import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.server.{BackgroundJobServer, JobActivator}
import org.jobrunr.storage.{InMemoryStorageProvider, StorageProvider}
import processors.{Executor, ExecutorImpl}

import scala.concurrent.ExecutionContext

trait JobRunrService {
  def enqueue(job: JobLambda): Unit
  def recurring(cronTime: String)(job: JobLambda): Unit
}

object JobRunrService {
  private val storageProvider: StorageProvider = new InMemoryStorageProvider()

  def initialize(implicit actor: ActorSystem[Nothing], context: ExecutionContext): Unit = {
    JobRunr.configure()
      .useStorageProvider(storageProvider)
      .useDefaultBackgroundJobServer()
      .useDashboard(8080)
      .initialize()
  }
}

class JobRunrServiceImpl extends JobRunrService {
  def enqueue(job: JobLambda): Unit = {
    BackgroundJob.enqueue(job)
  }

  def recurring(cronTime: String)(job: JobLambda): Unit = {
    BackgroundJob.scheduleRecurrently("crypto-notify", cronTime, job)
  }
}

//class MyJobActivator(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends JobActivator {
//  lazy val configuration: ConfigurationImpl = wire[ConfigurationImpl]
//  lazy val satangService: SatangServiceImpl = wire[SatangServiceImpl]
//  lazy val userService: UserServiceImpl = wire[UserServiceImpl]
//  lazy val lineService: LineServiceImpl = wire[LineServiceImpl]
//  lazy val executor: ExecutorImpl = wire[ExecutorImpl]
//
//  val usedServices: Map[Class[_], _] = Map(classOf[ConfigurationImpl] -> configuration,
//    classOf[SatangServiceImpl] -> satangService,
//    classOf[UserServiceImpl] -> userService,
//    classOf[LineServiceImpl] -> lineService,
//    classOf[ExecutorImpl] -> executor)
//
//  override def activateJob[T](`type`: Class[T]): T = {
//        usedServices.get(`type`.asInstanceOf[Class[_]]) match {
//          case Some(o) => o.asInstanceOf[T]
//          case _ => throw new Exception("Cannot not find class for jobactivator")
//        }
//    }
//}
