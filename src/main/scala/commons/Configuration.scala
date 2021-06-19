package commons

import com.typesafe.config.{Config, ConfigFactory}
import models.configuration.{AppConfig, FirebaseConfig, LineConfig, SatangConfig}

trait Configuration {
  val lineConfig: LineConfig
  val satangConfig: SatangConfig
  val firebaseConfig: FirebaseConfig
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
  private val firebaseSection = conf.getConfig("firebase")
  val appConfig: AppConfig = AppConfig(appSection.getInt("port"))
  val lineConfig: LineConfig = LineConfig(lineSection.getString("lineNotifyToken"), lineSection.getString("url"))
  val satangConfig: SatangConfig = SatangConfig(satangSection.getString("apiKey"), satangSection.getString("apiSecret"), satangSection.getString("userId"), satangSection.getString("url"))
  val firebaseConfig: FirebaseConfig = FirebaseConfig(firebaseSection.getString("configPath"))
}
