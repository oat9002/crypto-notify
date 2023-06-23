package models.configuration

final case class AppConfig(port: Int, mode: Mode)

enum Mode:
  case development, production
