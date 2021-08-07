package models.configuration

final case class AkkaConfig(quartz: Quartz)

final case class Quartz(defaultTimezone: String,
                  schedules: Map[String, Schedule])

final case class Schedule(description: String,
                    expression: String)


