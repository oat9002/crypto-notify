package commons

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonUtil {
  private val mapper: JsonMapper = JsonMapper.builder()
      .addModule(DefaultScalaModule)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()

  implicit class JsonSerialized(obj: Any) {
    def toJson: String = mapper.writeValueAsString(obj)
  }

  implicit class JsonDeserialize(content: String) {
    def toObject[T](clazz: Class[T]): T = mapper.readValue(content, clazz)
  }
}
