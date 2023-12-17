package controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives.{complete, get, onComplete, path, headerValueByName}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import processors.NotifyProcessor
import models.controller.NotifyResponse

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class NotifyController(using notifyProcessor: NotifyProcessor)(using system: ActorSystem[Nothing], context: ExecutionContext) extends FailFastCirceSupport {
  val route: Route = {
    path("notify") {
      get {
        headerValueByName("apiKey") { apiKey =>
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
