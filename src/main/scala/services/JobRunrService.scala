package services

import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import org.jobrunr.configuration.JobRunr
import org.jobrunr.jobs.lambdas.JobLambda
import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.server.{BackgroundJobServer, JobActivator}
import org.jobrunr.storage.{InMemoryStorageProvider, StorageProvider}

import scala.concurrent.ExecutionContext

trait JobRunrService {
  def enqueue(job: JobLambda): Unit
  def recurring(cronTime: String)(job: JobLambda): Unit
}

class JobRunrServiceImpl(implicit actor: ActorSystem[Nothing], context: ExecutionContext) extends JobRunrService {
  private val storageProvider: StorageProvider = new InMemoryStorageProvider()
  val jobActivator = new MyJobActivator(Map(classOf[JobRunrServiceImpl] -> this))

  JobRunr.configure()
    .useStorageProvider(storageProvider)
    .useBackgroundJobServer({
      val b = new BackgroundJobServer(storageProvider, jobActivator)
      b.start()
      b
    })
    .useDashboard(8080)
    .initialize()

  def enqueue(job: JobLambda): Unit = {
    BackgroundJob.enqueue(job)
  }

  def recurring(cronTime: String)(job: JobLambda): Unit = {
    BackgroundJob.scheduleRecurrently("crypto-notify", cronTime, job)
  }
}

class MyJobActivator(extra: Map[Class[_], _])(implicit actor: ActorSystem[Nothing], context: ExecutionContext) extends JobActivator {
  lazy val configuraiton: Configuration = wire[ConfigurationImpl]
  lazy val userService: UserService = wire[UserServiceImpl]
  lazy val lineService: LineService = wire[LineServiceImpl]

  val usedServices: Map[Class[_], _] = (Map(classOf[ConfigurationImpl] -> configuraiton,
    classOf[UserServiceImpl] -> userService,
    classOf[LineServiceImpl] -> lineService) ++ extra).toMap

  override def activateJob[T](`type`: Class[T]): T = {
        usedServices.get(`type`.asInstanceOf[Class[_]]) match {
          case Some(o) => o.asInstanceOf[T]
          case _ => throw new Exception("Cannot not find class for jobactivator")
        }
    }
}
