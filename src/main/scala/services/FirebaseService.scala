package services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.softwaremill.macwire.wire
import commons.{Configuration, ConfigurationImpl}
import models.configuration.FirebaseConfig

import java.io.FileInputStream
import scala.concurrent.Future

trait FirebaseService {

}

class FirebaseServiceImpl extends FirebaseService {

}

object FirebaseService {
  def initializeApp(firebaseConfig: FirebaseConfig): Unit = {
    val serviceAccount = new FileInputStream(firebaseConfig.configPath)
    val credentials = GoogleCredentials.fromStream(serviceAccount)
    val options = FirebaseOptions.builder()
      .setCredentials(credentials)
      .build()

    FirebaseApp.initializeApp(options)
  }
}
