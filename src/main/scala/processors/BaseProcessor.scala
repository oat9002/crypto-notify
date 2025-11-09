package processors

import actors.Command
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

trait BaseProcessor {

  def run(): Future[Boolean]
}
