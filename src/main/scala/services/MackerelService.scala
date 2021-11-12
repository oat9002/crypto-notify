package services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import commons.HttpResponseUtil.ToJsonString
import commons.JsonUtil.JsonSerialize
import models.mackerel.MackerelRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait MackerelService {
  def sendMeasurement(request: List[MackerelRequest]): Future[Boolean]
}

class MackerelServiceImpl(configuration: Configuration)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends MackerelService with LazyLogging {
  override def sendMeasurement(request: List[MackerelRequest]): Future[Boolean] = {
    val url = s"${configuration.mackerelConfig.url}/api/v0/services/${configuration.mackerelConfig.serviceName}/tsdb"
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, request.toJson),
      headers = Seq[HttpHeader](RawHeader("X-Api-Key", s"${configuration.mackerelConfig.apiKey}"))
    ))

    response.map {
      case HttpResponse(StatusCodes.OK, _, _, _) => true
      case HttpResponse(_, _, entity, _) =>
        entity.toJson.onComplete {
          case Success(Some(v)) => logger.error(s"Send measurement failed, ${request.toJson} : $v")
          case _ => logger.error(s"Send measurement failed, ${request.toJson}")
        }

        false
      case _ =>
        logger.error(s"Send measurement failed, name: ${request.toJson}")
        false
    }
  }
}
