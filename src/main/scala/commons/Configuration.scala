package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.{LineConfig, SatangConfig}

trait Configuration {
  val lineConfig: LineConfig
  val satangConfig: SatangConfig
}

class ConfigurationImpl extends Configuration {
  private val conf: Config = ConfigFactory.load()
  private val lineSection = conf.getConfig("line")
  private val satangSection = conf.getConfig("satang")
  val lineConfig: LineConfig = LineConfig(lineSection.getString("lineNotifyToken"), lineSection.getString("url"))
  val satangConfig: SatangConfig = SatangConfig(satangSection.getString("apiKey"), satangSection.getString("apiSecret"), satangSection.getString("userId"), satangSection.getString("urls"))
}
