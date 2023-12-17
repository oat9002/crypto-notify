package models.configuration

final case class AppConfig(port: Int, mode: Mode, useScheduler: Boolean, apiKey: String)

enum Mode:
  case development, production
