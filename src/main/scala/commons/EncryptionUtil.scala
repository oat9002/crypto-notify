package commons

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

trait EncryptionUtil {
  def generateHMAC512(message: String, key: String): String
}

class EncryptionUtilImpl extends EncryptionUtil {
  override def generateHMAC512(message: String, key: String): String = {
    val algorithm = "HmacSHA512"
    val secret = new SecretKeySpec(key.getBytes(), algorithm)
    val mac = Mac.getInstance(algorithm)

    mac.init(secret)

    val digest = mac.doFinal(message.getBytes())

    digest.map(d => String.format("%02x", d)).mkString("")
  }
}