package moe.isning.syncthing.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Polls the Web GUI until it is available (HTTP 2xx).
 * - Poll interval defaults to 150ms (matches previous behavior)
 * - Optional timeout: null means wait forever
 */
class WebGuiPoller(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    @OptIn(ExperimentalTime::class)
    suspend fun waitUntilAvailable(
        pollInterval: Duration = 150.milliseconds,
        timeout: Duration? = null,
    ): Boolean {
        val start = Clock.System.now().nanosecondsOfSecond
        while (true) {
            try {
                val response = client.get { url(baseUrl) }
                if (response.status.isSuccess()) return true
            } catch (_: Throwable) {
                // ignore, will retry
            }
            if (timeout != null) {
                val elapsed = (Clock.System.now().nanosecondsOfSecond - start) / 1_000_000_000.0
                if (elapsed >= timeout.inWholeSeconds) return false
            }
            delay(pollInterval)
        }
    }
}
