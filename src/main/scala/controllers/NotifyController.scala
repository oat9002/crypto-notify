package controllers

import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import org.apache.pekko.http.scaladsl.server.Directives.{complete, get, headerValueByName, onComplete, path}
import org.apache.pekko.http.scaladsl.server.Route
import commons.Configuration
import models.controller.NotifyResponse
import processors.NotifyProcessor
import validators.controllers.ApiKeyValidator
import scala.concurrent.duration.*

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class NotifyController(using notifyProcessor: NotifyProcessor, config: Configuration)(using
    system: ActorSystem[Nothing],
    context: ExecutionContext
) extends FailFastCirceSupport
    with ApiKeyValidator {
  protected val configuration: Configuration = config

  val route: Route = {
    path("notify") {
      withRequestTimeout(3.minutes) {
        get {
          validateApiKey {
            val result = notifyProcessor.run().map(NotifyResponse(_))

            onComplete(result) {
              case Success(value) => complete(OK, value)
              case Failure(ex)    => complete(InternalServerError, ex.getMessage)
            }
          }
        }
      }
    }
  }
}
