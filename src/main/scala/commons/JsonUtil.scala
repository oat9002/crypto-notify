package commons

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

trait JsonUtil {
  def serialize[T](obj: T): String
  def deserialize[T](content: String, c: Class[T]): T
}

class JsonUtilImpl() extends JsonUtil {
  private val mapper: JsonMapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build()

  def serialize[T](obj: T): String = mapper.writeValueAsString(obj)
  def deserialize[T](content: String, c: Class[T]): T = mapper.readValue(content, c)
}

