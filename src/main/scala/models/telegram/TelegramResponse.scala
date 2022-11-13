package models.telegram

import io.circe._
import io.circe.generic.semiauto._

case class TelegramResponse(ok: Boolean, result: Message)

object TelegramResponse {
  given Encoder[TelegramResponse] = deriveEncoder[TelegramResponse]
  given Decoder[TelegramResponse] = deriveDecoder[TelegramResponse]
}

case class Message(messageId: Long, from: From, chat: Chat, date: Long, text: String)

object Message {
  given Encoder[Message] =
    Encoder.forProduct5("message_id", "from", "chat", "date", "text")(m =>
      (m.messageId, m.from, m.chat, m.date, m.text)
    )
  given Decoder[Message] =
    Decoder.forProduct5("message_id", "from", "chat", "date", "text")(Message.apply)
}

case class From(id: Long, isBot: Boolean, firstName: String, username: String)

object From {
  given Encoder[From] = Encoder.forProduct4("id", "is_bot", "first_name", "username")(f =>
    (f.id, f.isBot, f.firstName, f.username)
  )
  given Decoder[From] = Decoder.forProduct4("id", "is_bot", "first_name", "username")(From.apply)
}

case class Chat(id: Long, firstName: String, `type`: String)

object Chat {
  given Encoder[Chat] =
    Encoder.forProduct3("id", "first_name", "type")(c => (c.id, c.firstName, c.`type`))
  given Decoder[Chat] = Decoder.forProduct3("id", "first_name", "type")(Chat.apply)
}
