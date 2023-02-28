package models.configuration

final case class MackerelConfig(
    enabled: Boolean,
    apiKey: String,
    serviceName: String
)
