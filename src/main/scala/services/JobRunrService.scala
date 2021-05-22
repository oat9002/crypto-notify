package services

import com.softwaremill.macwire.wire
import org.jobrunr.configuration.JobRunr
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.storage.{InMemoryStorageProvider, StorageProvider}

trait JobRunrService {
  def enqueue(execute: Unit => Unit): Unit
  def recuring(cronTime: String)(execute: Unit => Unit): Unit
  def initialize(): Unit
}

class JobRunrServiceImpl extends JobRunrService {
  private val storageProvider: StorageProvider = new InMemoryStorageProvider()
  private val jobScheduler: JobScheduler = wire[JobScheduler]

  def enqueue(execute: Unit => Unit): Unit = {
    jobScheduler.enqueue(() => execute())
  }

  def recuring(cronTime: String)(execute: Unit => Unit): Unit = {
    jobScheduler.scheduleRecurrently(cronTime, () =>  execute())
  }

  def initialize(): Unit = {
    JobRunr.configure()
      .useStorageProvider(storageProvider)
      .useDefaultBackgroundJobServer()
      .useDashboard(8080)
      .initialize()
  }
}
