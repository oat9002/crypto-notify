package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.{AkkaConfig, AppConfig, BinanceConfig, BscScanConfig, LineConfig, MackerelConfig, Quartz, SatangConfig, Schedule, TerraConfig}


trait Configuration {
  val appConfig: AppConfig
  val lineConfig: LineConfig
  val satangConfig: SatangConfig
  val bscScanConfig: BscScanConfig
  val akkaConfig: AkkaConfig
  val mackerelConfig: MackerelConfig
  val binanceConfig: BinanceConfig
  val terraConfig: TerraConfig
}

class ConfigurationImpl extends Configuration {
  private val conf: Config = {
    val baseConfig = ConfigFactory.load()

    ConfigFactory.load("application.local").withFallback(baseConfig)
  }
  private val appSection = conf.getConfig("app")
  private val lineSection = conf.getConfig("line")
  private val satangSection = conf.getConfig("satang")
  private val bscScanSection = conf.getConfig("bscScan")
  private val akkaSection = conf.getConfig("akka")
  private val mackerelSection = conf.getConfig("mackerel")
  private val binanceSection = conf.getConfig("binance")
  private val terraSection = conf.getConfig("terra")
  lazy val appConfig: AppConfig = AppConfig(appSection.getInt("port"))
  lazy val lineConfig: LineConfig = LineConfig(lineSection.getString("lineNotifyToken"), lineSection.getString("url"))
  lazy val satangConfig: SatangConfig = SatangConfig(satangSection.getString("apiKey"), satangSection.getString("apiSecret"), satangSection.getString("userId"), satangSection.getString("url"))
  lazy val bscScanConfig: BscScanConfig = BscScanConfig(bscScanSection.getString("url"), bscScanSection.getString("apiKey"), bscScanSection.getString("address"))
  lazy val akkaConfig: AkkaConfig = AkkaConfig(Quartz(akkaSection.getConfig("quartz").getString("defaultTimezone"),{
    val sch = akkaSection.getConfig("quartz").getConfig("schedules")
    val every3hours = sch.getConfig("Every3hours")
    val every1Minute = sch.getConfig("Every1Minute")
    val every10Secs = sch.getConfig("Every10Seconds")
    val notify = sch.getConfig("Notify")
    val healthCheck = sch.getConfig("HealthCheck")
    Map("Every3hours" -> Schedule(every3hours.getString("description"), every3hours.getString("expression")),
      "Every1Minute" -> Schedule(every1Minute.getString("description"), every1Minute.getString("expression")),
      "Every10Seconds" -> Schedule(every10Secs.getString("description"), every10Secs.getString("expression")),
      "Notify" -> Schedule(notify.getString("description"), notify.getString("expression")),
      "HealthCheck" -> Schedule(healthCheck.getString("description"), healthCheck.getString("expression"))
    )
  }))
  lazy val mackerelConfig: MackerelConfig = MackerelConfig(mackerelSection.getString("url"), mackerelSection.getString("apiKey"), mackerelSection.getString("serviceName"))
  lazy val binanceConfig: BinanceConfig = BinanceConfig(binanceSection.getString("url"), binanceSection.getString("apiKey"), binanceSection.getString("secretKey"))
  lazy val terraConfig: TerraConfig = TerraConfig(terraSection.getString("url"), terraSection.getString("twoPointOUrl"), terraSection.getString("address"))
}
