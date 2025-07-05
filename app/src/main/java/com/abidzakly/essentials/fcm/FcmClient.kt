package com.abidzakly.essentials.fcm

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FCMClient(private val projectId: String) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun sendNotification(
        accessToken: String,
        targetToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Result<FCMResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("https://fcm.googleapis.com/v1/projects/$projectId/messages:send") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(
                    FCMMessage(
                        message = Message(
                            token = targetToken,
                            notification = Notification(title, body),
                            data = data.takeIf { it.isNotEmpty() }
                        )
                    )
                )
            }
            Result.success(response.body<FCMResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendToTopic(
        accessToken: String,
        topic: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Result<FCMResponse> = withContext(Dispatchers.IO) {
        try {
            val response = client.post("https://fcm.googleapis.com/v1/projects/$projectId/messages:send") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(
                    FCMMessage(
                        message = Message(
                            topic = topic,
                            notification = Notification(title, body),
                            data = data.takeIf { it.isNotEmpty() }
                        )
                    )
                )
            }
            Result.success(response.body<FCMResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class FCMMessage(
    val message: Message
)

@Serializable
data class Message(
    val token: String? = null,
    val topic: String? = null,
    val notification: Notification,
    val data: Map<String, String>? = null
)

@Serializable
data class Notification(
    val title: String,
    val body: String
)

@Serializable
data class FCMResponse(
    val name: String
)