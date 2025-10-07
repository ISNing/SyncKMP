package moe.isning.synckmp.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Configuration for connecting to a local Syncthing instance.
 * - baseUrl: full base URL, e.g. "https://127.0.0.1:8384"
 * - apiKey: X-API-Key from Syncthing configuration
 */
data class SyncApiConfig(
    val baseUrl: String,
    val apiKey: String,
)

/**
 * Factory to build a KMP-friendly Ktor HttpClient for Syncthing.
 *
 * Notes about TLS/certificate pinning:
 * - This shared code avoids JVM/Android-specific APIs; to pin a self-signed Syncthing cert on
 *   each platform, inject additional engine configuration via [configure]. For example,
 *   on Android you can select OkHttp/Darwin engines and add platform-native cert pinning.
 * - By default, this uses platform defaults and does NOT disable hostname verification.
 */
object SyncHttpClientFactory {

    fun create(
        config: SyncApiConfig,
        enableLogging: Boolean = false,
        // Hook for platform-specific TLS or engine config from caller
        configure: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        if (enableLogging) {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.INFO
            }
        }
        defaultRequest {
            headers.append("Authorization", "Bearer ${config.apiKey}")
            headers.append("Accept", ContentType.Application.Json.toString())
            contentType(ContentType.Application.Json)
        }
        configure()
    }
}
