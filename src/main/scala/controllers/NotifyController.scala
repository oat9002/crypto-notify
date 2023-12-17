package controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives.{complete, get, headerValueByName, onComplete, path}
import akka.http.scaladsl.server.Route
import commons.Configuration
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import processors.NotifyProcessor
import models.controller.NotifyResponse
import validators.controllers.ApiKeyValidator

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class NotifyController(using notifyProcessor: NotifyProcessor, val configuration: Configuration)(using system: ActorSystem[Nothing], context: ExecutionContext) extends FailFastCirceSupport with ApiKeyValidator {
  val route: Route = {
    path("notify") {
      get {
        validateApiKey {
          val result = notifyProcessor.run().map(NotifyResponse(_))

          onComplete(result) {
            case Success(value) => complete(OK, value)
            case Failure(ex) => complete(InternalServerError, ex.getMessage)
          }
        }
      }
    }
  }
}
