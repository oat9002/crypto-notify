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
  def postFormData[Res : ClassTag](url: String, request: Map[String, String], header: Map[String, String] = Map()) : Future[Either[String, Res]]
}

class HttpClientImpl(implicit system: ActorSystem[Nothing], ec: ExecutionContext) extends HttpClient {
  val backend: SttpBackend[Future, AkkaStreams with capabilities.WebSockets] = AkkaHttpBackend.usingActorSystem(system.classicSystem)

  override def get[Req : ClassTag, Res : ClassTag](url: String, request: Option[Req] = None, header: Map[String, String]): Future[Either[String, Res]] = {
    val response = request match {
      case Some(req) => basicRequest
        .contentType("application/json")
        .body(req.toJson)
        .headers(header)
        .get(uri"$url")
        .send(backend)
      case _ => basicRequest
        .contentType("application/json")
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
      .contentType("application/json")
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

  override def postFormData[Res: ClassTag](url: String, request: Map[String, String], header: Map[String, String] = Map()): Future[Either[String, Res]] = {
    val body = request.map(x => multipart(x._1, x._2)).to(Seq)
    val response = basicRequest
      .multipartBody(body)
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
