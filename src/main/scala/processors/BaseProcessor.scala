package processors

import actors.Command
import akka.actor.typed.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

trait BaseProcessor {
  
  def run(): Future[Boolean]
}
