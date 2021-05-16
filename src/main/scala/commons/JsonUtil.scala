package commons

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

trait JsonUtil {
  def serialize[T](obj: T): String
  def deserialize[T](content: String): T
}

class JsonUtilImpl(mapper: ObjectMapper) extends JsonUtil {
  def serialize[T](obj: T): String = mapper.writeValueAsString(obj)
  def deserialize[T](content: String): T = mapper.readValue(content, classOf[T])
}

object JsonUtil {
  def apply(): JsonUtil = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    new JsonUtilImpl(mapper)
  }
}
