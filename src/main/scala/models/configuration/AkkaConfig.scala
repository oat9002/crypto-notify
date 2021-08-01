package models.configuration

case class AkkaConfig(quartz: Quartz)

case class Quartz(defaultTimezone: String,
                  schedules: Map[String, Schedule])

case class Schedule(description: String,
                    expression: String)


