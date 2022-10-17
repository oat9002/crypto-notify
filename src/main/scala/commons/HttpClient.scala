package commons

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons.*
import io.circe.*
import io.circe.generic.semiauto._
import sttp.*
import sttp.capabilities.akka.AkkaStreams
import sttp.client3.*
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.circe.asJson

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait HttpClient {
  def get[Res](url: String, header: Map[String, String] = Map())(using
      decoder: Decoder[Res]
  ): Future[Either[String, Res]]
  def post[Req, Res](
      url: String,
      request: Req,
      header: Map[String, String] = Map()
  )(using
      encoder: Encoder[Req],
      decoder: Decoder[Res]
  ): Future[Either[String, Res]]
  def postFormData[Res](
      url: String,
      request: Map[String, String],
      header: Map[String, String] = Map()
  )(using
      decoder: Decoder[Res]
  ): Future[Either[String, Res]]
}

class HttpClientImpl(using
    system: ActorSystem[Nothing],
    ec: ExecutionContext
) extends HttpClient
    with LazyLogging {
  val backend: SttpBackend[Future, AkkaStreams with capabilities.WebSockets] =
    AkkaHttpBackend.usingActorSystem(system.classicSystem)
  override def get[Res](url: String, header: Map[String, String])(using
      decoder: Decoder[Res]
  ): Future[Either[String, Res]] = {

    val response = basicRequest
      .contentType("application/json")
      .headers(header)
      .get(uri"$url")
      .response(asJson[Res])
      .send(backend)

    response.map { x =>
      x.body match {
        case Left(error)     => Left(error.getMessage)
        case Right(response) => Right(response)
      }
    }
  }

  override def post[Req, Res](
      url: String,
      request: Req,
      header: Map[String, String] = Map()
  )(using
      encoder: Encoder[Req],
      decoder: Decoder[Res]
  ): Future[Either[String, Res]] = {

    val response = basicRequest
      .contentType("application/json")
      .body(encoder(request).spaces2)
      .headers(header)
      .post(uri"$url")
      .response(asJson[Res])
      .send(backend)

    response.map { x =>
      x.body match {
        case Left(error)     => Left(error.getMessage)
        case Right(response) => Right(response)
      }
    }
  }

  override def postFormData[Res](
      url: String,
      request: Map[String, String],
      header: Map[String, String] = Map()
  )(using
      decoder: Decoder[Res]
  ): Future[Either[String, Res]] = {

    val body = request.map(x => multipart(x._1, x._2)).to(Seq)
    val response = basicRequest
      .multipartBody(body)
      .headers(header)
      .post(uri"$url")
      .response(asJson[Res])
      .send(backend)

    response.map { x =>
      x.body match {
        case Left(error)     => Left(error.getMessage)
        case Right(response) => Right(response)
      }
    }
  }
}
