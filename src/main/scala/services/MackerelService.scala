package services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import com.typesafe.scalalogging.LazyLogging
import commons.Configuration
import commons.JsonUtil.JsonSerialized
import models.mackerel.MackerelRequest

import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

trait MackerelService {
  def sendMeasurement(name: String, value: Int): Future[Boolean]
}

class MackerelServiceImpl(configuration: Configuration)(implicit system: ActorSystem[Nothing], context: ExecutionContext) extends MackerelService with LazyLogging {
  override def sendMeasurement(name: String, value: Int): Future[Boolean] = {
    val url = s"${configuration.mackerelConfig.url}/api/v0/services/goldprice-tracking-service/tsdb"
    val now = LocalDateTime.now(ZoneId.of("Asia/Bangkok")).getNano
    val response = Http().singleRequest(HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      entity = HttpEntity(ContentTypes.`application/json`, MackerelRequest(name, now, value).toJson)
    ))

    response.map {
      case HttpResponse(StatusCodes.OK, _, _, _) => true
      case _ =>
        logger.error(s"Send measurement failed, name: $name, value: $value")
        false
    }
  }
}
