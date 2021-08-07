package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.{AkkaConfig, AppConfig, BscScanConfig, LineConfig, MackerelConfig, Quartz, SatangConfig, Schedule}


trait Configuration {
  val appConfig: AppConfig
  val lineConfig: LineConfig
  val satangConfig: SatangConfig
  val bscScanConfig: BscScanConfig
  val akkaConfig: AkkaConfig
  val mackerelConfig: MackerelConfig
}

class ConfigurationImpl extends Configuration {
  private val conf: Config = {
    val c = ConfigFactory.load()
    val env = c.getConfig("app").getString("env").toLowerCase.trim
    if ("development".equals(env)) {
      val toReturn = ConfigFactory.load("application.local")
      ConfigFactory.invalidateCaches()
      toReturn
    } else {
      c
    }
  }
  private val appSection = conf.getConfig("app")
  private val lineSection = conf.getConfig("line")
  private val satangSection = conf.getConfig("satang")
  private val bscScanSection = conf.getConfig("bscScan")
  private val akkaSection = conf.getConfig("akka")
  private val mackerelSection = conf.getConfig("mackerel")
  lazy val appConfig: AppConfig = AppConfig(appSection.getInt("port"))
  lazy val lineConfig: LineConfig = LineConfig(lineSection.getString("lineNotifyToken"), lineSection.getString("url"))
  lazy val satangConfig: SatangConfig = SatangConfig(satangSection.getString("apiKey"), satangSection.getString("apiSecret"), satangSection.getString("userId"), satangSection.getString("url"))
  lazy val bscScanConfig: BscScanConfig = BscScanConfig(bscScanSection.getString("url"), bscScanSection.getString("apiKey"), bscScanSection.getString("address"))
  lazy val akkaConfig: AkkaConfig = AkkaConfig(Quartz(akkaSection.getConfig("quartz").getString("defaultTimezone"),{
    val sch = akkaSection.getConfig("quartz").getConfig("schedules")
    val every3hours = sch.getConfig("Every3hours")
    val every10Secs = sch.getConfig("Every10Seconds")
    val custom = sch.getConfig("Custom")
    Map("Every3hours" -> Schedule(every3hours.getString("description"),every3hours.getString("expression")),
      "Every10Seconds" -> Schedule(every10Secs.getString("description"), every10Secs.getString("expression")),
      "Custom" -> Schedule(custom.getString("description"), custom.getString("expression")))
  }))
  lazy val mackerelConfig: MackerelConfig = MackerelConfig(mackerelSection.getString("url"), mackerelSection.getString("apiKey"))
}
