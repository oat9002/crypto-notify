package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.*

import scala.util.{Success, Try}

trait Configuration {
  lazy val appConfig: AppConfig
  lazy val lineConfig: Option[LineConfig]
  lazy val telegramConfig: Option[TelegramConfig]
  lazy val satangConfig: SatangConfig
  lazy val akkaConfig: AkkaConfig
  lazy val etherScanConfig: Option[EtherScanConfig]
  lazy val mackerelConfig: Option[MackerelConfig]
  lazy val binanceConfig: Option[BinanceConfig]
  lazy val terraConfig: Option[TerraConfig]
  lazy val bitcoinConfig: Option[BitcoinConfig]
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
  private val etherScanSection = conf.getConfig("etherScan")
  private val mackerelSection = conf.getConfig("mackerel")
  private val binanceSection = conf.getConfig("binance")
  private val terraSection = conf.getConfig("terra")
  private val telegramSection = conf.getConfig("telegram")
  private val bitcoinSection = conf.getConfig("bitcoin")
  lazy val appConfig: AppConfig = AppConfig(
    appSection.getInt("port"),
    if (appSection.getString("mode") == "production") Mode.production else Mode.development,
    appSection.getBoolean("useScheduler"),
    appSection.getString("apiKey")
  )
  lazy val lineConfig: Option[LineConfig] = Try(
    LineConfig(
      lineSection.getString("lineNotifyToken")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
  lazy val satangConfig: SatangConfig = SatangConfig(
    satangSection.getString("apiKey"),
    satangSection.getString("apiSecret"),
    satangSection.getString("userId")
  )
  lazy val etherScanConfig: Option[EtherScanConfig] = Try(
    EtherScanConfig(
      etherScanSection.getString("apiKey"),
      etherScanSection.getString("address")
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
      mackerelSection.getBoolean("enabled"),
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
  lazy val telegramConfig: Option[TelegramConfig] = Try(
    TelegramConfig(
      telegramSection.getString("botToken"),
      telegramSection.getString("chatId")
    )
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
  lazy val bitcoinConfig: Option[BitcoinConfig] = Try(
    BitcoinConfig(bitcoinSection.getString("addresses").split(",").map(_.trim).toList)
  ) match {
    case Success(v) => Some(v)
    case _          => None
  }
}
