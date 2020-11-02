package de.innfactory.auth.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.{FirebaseApp, FirebaseOptions}

object FirebaseBase {

    def instantiateFirebase(serviceAccountJsonFilepath: String): FirebaseApp = {
      val serviceAccount = getClass.getClassLoader.getResourceAsStream(serviceAccountJsonFilepath)

      val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build

      FirebaseApp.initializeApp(options)
    }

    def deleteFirebase(): Unit =
      FirebaseApp.getInstance().delete()

}
