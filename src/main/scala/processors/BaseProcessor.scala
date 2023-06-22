package processors

import scala.concurrent.Future

trait BaseProcessor {
  def run(): Future[Boolean]
}
