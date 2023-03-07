package commons

import akka.actor.typed.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import commons._
import io.circe._
import io.circe.generic.semiauto._
import sttp._
import sttp.capabilities.akka.AkkaStreams
import sttp.client3._
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.circe.asJson

import concurrent.duration.DurationInt
import retry.Success._
import sttp.model.StatusCode

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

    val response = retryHttpCall(basicRequest
      .contentType("application/json")
      .headers(header)
      .get(uri"$url")
      .response(asJson[Res])
      .send(backend))

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

    val response = retryHttpCall(basicRequest
      .contentType("application/json")
      .body(encoder(request).spaces2)
      .headers(header)
      .post(uri"$url")
      .response(asJson[Res])
      .send(backend))

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
    val response = retryHttpCall(basicRequest
      .multipartBody(body)
      .headers(header)
      .post(uri"$url")
      .response(asJson[Res])
      .send(backend))

    response.map { x =>
      x.body match {
        case Left(error)     => Left(error.getMessage)
        case Right(response) => Right(response)
      }
    }
  }

  private def retryHttpCall[Res](f: Future[Response[Either[ResponseException[String, Error], Res]]]): Future[Response[Either[ResponseException[String, Error], Res]]] = {
    val successPolicy = (res: Response[Either[ResponseException[String, Error], Res]]) =>
      res.code match
        case StatusCode.Ok => true
        case StatusCode.BadRequest => true
        case StatusCode.Unauthorized => true
        case StatusCode.Forbidden => true
        case StatusCode.NotFound => true
        case _ => false

    given retry.Success[Response[Either[ResponseException[String, Error], Res]]] = retry.Success[Response[Either[ResponseException[String, Error], Res]]](successPolicy)

    retry.Backoff(3, 1.second).apply(f)
  }
}
