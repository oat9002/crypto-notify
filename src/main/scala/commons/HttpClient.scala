package commons

import akka.actor.typed.ActorSystem
import commons.JsonUtil._
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.client3._
import sttp.client3.akkahttp.AkkaHttpBackend

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait HttpClient {
  def get[Req : ClassTag, Res : ClassTag](url: String, request: Option[Req] = None, header: Map[String, String] = Map()): Future[Either[String, Res]]
  def post[Req : ClassTag, Res : ClassTag](url: String, request: Req, header: Map[String, String] = Map()): Future[Either[String, Res]]
}

class HttpClientImpl(implicit system: ActorSystem[Nothing], ec: ExecutionContext) extends HttpClient {
  val backend: SttpBackend[Future, AkkaStreams with capabilities.WebSockets] = AkkaHttpBackend.usingActorSystem(system.classicSystem)

  override def get[Req : ClassTag, Res : ClassTag](url: String, request: Option[Req] = None, header: Map[String, String]): Future[Either[String, Res]] = {
    val response = request match {
      case Some(req) => basicRequest
        .body(req.toJson)
        .headers(header)
        .get(uri"$url")
        .send(backend)
      case _ => basicRequest
        .headers(header)
        .get(uri"$url")
        .send(backend)
    }

    response.map { x =>
      x.body match {
        case Left(error) => Left(error)
        case Right(responseJson) => Right(responseJson.toObject[Res])
      }
    }
  }

  override def post[Req : ClassTag, Res : ClassTag](url: String, request: Req, header: Map[String, String] = Map()): Future[Either[String, Res]] = {
    val response = basicRequest
      .body(request.toJson)
      .headers(header)
      .post(uri"$url")
      .send(backend)

    response.map { x =>
      x.body match {
        case Left(error) => Left(error)
        case Right(responseJson) => Right(responseJson.toObject[Res])
      }
    }
  }
}
