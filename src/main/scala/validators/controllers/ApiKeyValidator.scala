package validators.controllers

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.headerValueByName
import org.apache.pekko.http.scaladsl.server.{
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
