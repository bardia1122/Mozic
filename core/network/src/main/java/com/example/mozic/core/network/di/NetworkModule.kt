package com.example.mozic.core.network.di

import com.example.mozic.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MS = 15_000L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) { json(json) }

        // C5's chat WS client (ChatWebSocketClient) reuses this same client
        // rather than a second one — the WS handshake auth is a `?token=`
        // query param (backend/PROTOCOL.md), not a header, so there's no
        // conflict with the `apikey` default header below.
        install(WebSockets)

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = REQUEST_TIMEOUT_MS
        }

        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { header -> header == "apikey" || header == "Authorization" }
        }

        defaultRequest {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        }
    }
}
