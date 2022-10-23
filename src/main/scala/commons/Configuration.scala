package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.{
  AkkaConfig,
  AppConfig,
  BinanceConfig,
  BscScanConfig,
  LineConfig,
  MackerelConfig,
  Quartz,
  SatangConfig,
  Schedule,
  TerraConfig
}

import scala.util.{Success, Try}

trait Configuration {
  lazy val appConfig: AppConfig
  lazy val lineConfig: LineConfig
  lazy val satangConfig: SatangConfig
  lazy val akkaConfig: AkkaConfig
  lazy val bscScanConfig: Option[BscScanConfig]
  lazy val mackerelConfig: Option[MackerelConfig]
  lazy val binanceConfig: Option[BinanceConfig]
  lazy val terraConfig: Option[TerraConfig]
}

class ConfigurationImpl extends Configuration {
  private val conf: Config = {
    val baseConfig = ConfigFactory.load()

    ConfigFactory.load("application.local").withFallback(baseConfig)
  }
  private val appSection = conf.getConfig("app")
  private val lineSection = conf.getConfig("line")
  private val satangSection = conf.getConfig("satang")
  private val akkaSection = conf.getConfig("akka")
  private val bscScanSection = conf.getConfig("bscScan")
  private val mackerelSection = conf.getConfig("mackerel")
  private val binanceSection = conf.getConfig("binance")
  private val terraSection = conf.getConfig("terra")
  lazy val appConfig: AppConfig = AppConfig(appSection.getInt("port"))
  lazy val lineConfig: LineConfig = LineConfig(
    lineSection.getString("lineNotifyToken")
  )
  lazy val satangConfig: SatangConfig = SatangConfig(
    satangSection.getString("apiKey"),
    satangSection.getString("apiSecret"),
    satangSection.getString("userId")
  )
  lazy val bscScanConfig: Option[BscScanConfig] = Try(
    BscScanConfig(
      bscScanSection.getString("apiKey"),
      bscScanSection.getString("address")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }

  lazy val akkaConfig: AkkaConfig = AkkaConfig(
    Quartz(
      akkaSection.getConfig("quartz").getString("defaultTimezone"), {
        val sch = akkaSection.getConfig("quartz").getConfig("schedules")
        val every3hours = sch.getConfig("Every3hours")
        val every1Minute = sch.getConfig("Every1Minute")
        val every10Secs = sch.getConfig("Every10Seconds")
        val notify = sch.getConfig("Notify")
        val healthCheck = sch.getConfig("HealthCheck")
        Map(
          "Every3hours" -> Schedule(
            every3hours.getString("description"),
            every3hours.getString("expression")
          ),
          "Every1Minute" -> Schedule(
            every1Minute.getString("description"),
            every1Minute.getString("expression")
          ),
          "Every10Seconds" -> Schedule(
            every10Secs.getString("description"),
            every10Secs.getString("expression")
          ),
          "Notify" -> Schedule(
            notify.getString("description"),
            notify.getString("expression")
          ),
          "HealthCheck" -> Schedule(
            healthCheck.getString("description"),
            healthCheck.getString("expression")
          )
        )
      }
    )
  )
  lazy val mackerelConfig: Option[MackerelConfig] = Try(
    MackerelConfig(
      mackerelSection.getString("apiKey"),
      mackerelSection.getString("serviceName")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
  lazy val binanceConfig: Option[BinanceConfig] = Try(
    BinanceConfig(
      binanceSection.getString("apiKey"),
      binanceSection.getString("secretKey")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
  lazy val terraConfig: Option[TerraConfig] = Try(
    TerraConfig(
      terraSection.getString("address")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
}
