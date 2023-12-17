package validators.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.headerValueByName
import akka.http.scaladsl.server.{
  AuthenticationFailedRejection,
  Directive0,
  Directives,
  ValidationRejection
}

trait ApiKeyValidator extends Directives with BaseValidator {
  def validateApiKey: Directive0 = {
    headerValueByName("apiKey").flatMap { apiKey =>
      if (configuration.appConfig.apiKey.equals(apiKey)) {
        pass
      } else {
        complete(StatusCodes.Unauthorized, "apiKey is invalid")
      }
    }
  }
}
