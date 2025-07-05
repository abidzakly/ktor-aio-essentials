package com.abidzakly.essentials.fcm

import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class FCMTokenHelper {

    /**
     * Get access token using service account JSON file from assets
     */
    suspend fun getAccessToken(serviceAccountInputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            val credentials = GoogleCredentials
                .fromStream(serviceAccountInputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        }
    }

    /**
     * Alternative: Get access token using service account JSON string
     */
    suspend fun getAccessToken(serviceAccountJson: String): String {
        return withContext(Dispatchers.IO) {
            val credentials = GoogleCredentials
                .fromStream(serviceAccountJson.byteInputStream())
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        }
    }
}