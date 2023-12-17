package validators.controllers

import akka.http.scaladsl.server.Directives.headerValueByName
import akka.http.scaladsl.server.{Directive0, Directives, ValidationRejection, AuthenticationFailedRejection}

trait ApiKeyValidator extends Directives with BaseValidator {
  def validateApiKey: Directive0 = {
    headerValueByName("apiKey").flatMap { apiKey =>
      if (configuration.appConfig.apiKey.equals(apiKey)) {
        pass
      } else {
        reject(ValidationRejection("apiKey is invalid"))
      }
    }
  }
}
