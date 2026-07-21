package com.example.mozic.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class UploadAvatarResponseDto(@SerialName("avatarUrl") val avatarUrl: String)

/**
 * Avatar upload/removal proxied through the custom chat backend
 * (`backend/python/main.py`'s `/avatar` routes) instead of calling Supabase
 * Storage directly. On this project, Storage's own RLS-based role resolution
 * for authenticated writes doesn't work — confirmed live: the exact same
 * user JWT that succeeds against every PostgREST write 403s against Storage
 * even with verified-correct policies (a platform-level issue, not a policy
 * bug). The backend already validates this same token for the WS handshake
 * and writes with the secret key server-side, which is confirmed to work.
 * Same `CHAT_WS_HOST` the WS client uses — plain `http://`, not `https://`,
 * same cleartext-traffic exemption `ChatWebSocketClient`'s `ws://` needs.
 */
@Singleton
class BackendProfileApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun uploadAvatar(accessToken: String, bytes: ByteArray, contentType: String): String {
        val response: UploadAvatarResponseDto = client.post("http://${BuildConfig.CHAT_WS_HOST}/avatar") {
            bearerAuth(accessToken)
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }.body()
        return response.avatarUrl
    }

    suspend fun removeAvatar(accessToken: String) {
        client.delete("http://${BuildConfig.CHAT_WS_HOST}/avatar") {
            bearerAuth(accessToken)
        }
    }
}
