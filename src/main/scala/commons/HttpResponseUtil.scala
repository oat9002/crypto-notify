package commons

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ResponseEntity

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

object HttpResponseUtil {
  val serializeTimeout: FiniteDuration = 5.seconds

  implicit class ToJsonString(entity: ResponseEntity) {
    def toJsonString(implicit context: ExecutionContext, actor: ActorSystem[Nothing]): Future[Option[String]] = entity.toStrict(serializeTimeout).map(e => e.getData()).map(data => Some(data.utf8String))
  }
}
