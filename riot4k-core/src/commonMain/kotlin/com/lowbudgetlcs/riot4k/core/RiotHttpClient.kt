package com.lowbudgetlcs.riot4k.core

import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import com.lowbudgetlcs.riot4k.core.ratelimit.HeaderRateLimiter
import com.lowbudgetlcs.riot4k.core.ratelimit.RateLimitHeaders
import com.lowbudgetlcs.riot4k.core.ratelimit.RateLimiter
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.Route
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Transport layer shared by every endpoint: authentication, routing, rate
 * limiting, and retries. Only 429s, 5xx responses, and transport errors are
 * retried (honoring `Retry-After`); all other 4xx responses fail immediately.
 *
 * The [rateLimiter] and [httpClient] parameters exist for tests (MockEngine)
 * and advanced consumers (custom engines/plugins); the defaults are production-ready.
 */
public class RiotHttpClient(
    private val config: Riot4KConfig,
    private val rateLimiter: RateLimiter =
        HeaderRateLimiter(config.clock, config.burstFactor, config.durationOverheadMs),
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    /**
     * Executes an authenticated GET against `pathSegments` (individually URL-encoded)
     * on [route]'s host and decodes the body as [T]. [methodId] (e.g.
     * `"account-v1.getByRiotId"`) keys the per-method rate-limit bucket.
     */
    public suspend inline fun <reified T : Any> get(
        route: Route,
        methodId: String,
        vararg pathSegments: String,
    ): RiotResult<T> = when (val outcome = execute(route, methodId, pathSegments)) {
        is RequestOutcome.Ok -> try {
            RiotResult.Success(outcome.response.body<T>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RiotResult.Failure(
                statusCode = outcome.response.status.value,
                retries = outcome.retries,
                rateLimitType = null,
                message = "Failed to decode response body: ${e.message}",
                cause = e,
            )
        }
        is RequestOutcome.NotFound -> RiotResult.NotFound
        is RequestOutcome.Failed -> outcome.failure
    }

    @PublishedApi
    internal suspend fun execute(
        route: Route,
        methodId: String,
        pathSegments: Array<out String>,
    ): RequestOutcome {
        val baseUrl = config.baseUrlTemplate.replace("{route}", route.subdomain)
        var attempt = 0
        while (true) {
            rateLimiter.acquire(route.subdomain, methodId)

            var cause: Throwable? = null
            val response: HttpResponse? = try {
                httpClient.get(baseUrl) {
                    url { appendPathSegments(*pathSegments) }
                    header(RIOT_KEY_HEADER, config.apiKey)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                cause = e
                null
            }

            if (response != null) {
                rateLimiter.onResponse(route.subdomain, methodId, response.status.value) { name ->
                    response.headers[name]
                }
            }

            val status = response?.status?.value
            when {
                status == HTTP_NO_CONTENT || status == HTTP_NOT_FOUND -> return RequestOutcome.NotFound
                status != null && status in SUCCESS_RANGE -> return RequestOutcome.Ok(response, attempt)
                status == null || status == HTTP_TOO_MANY_REQUESTS || status in SERVER_ERROR_RANGE -> {
                    // Retryable: transport error, 429, or 5xx.
                    val rateLimitType = response?.headers?.get(RateLimitHeaders.RATE_LIMIT_TYPE)
                    if (attempt >= config.maxRetries) {
                        return RequestOutcome.Failed(
                            RiotResult.Failure(
                                statusCode = status,
                                retries = attempt,
                                rateLimitType = rateLimitType,
                                message = failureMessage(status, attempt, cause),
                                cause = cause,
                            ),
                        )
                    }
                    delay(retryDelayMs(response, attempt))
                    attempt++
                }
                else -> return RequestOutcome.Failed(
                    RiotResult.Failure(
                        statusCode = status,
                        retries = attempt,
                        rateLimitType = null,
                        message = failureMessage(status, attempt, cause = null),
                        cause = null,
                    ),
                )
            }
        }
    }

    private fun retryDelayMs(response: HttpResponse?, attempt: Int): Long {
        val retryAfterSeconds = response?.headers?.get(RateLimitHeaders.RETRY_AFTER)?.trim()?.toLongOrNull()
        return retryAfterSeconds?.times(MS_PER_SECOND) ?: (INITIAL_BACKOFF_MS shl attempt)
    }

    private fun failureMessage(status: Int?, retries: Int, cause: Throwable?): String = when {
        status != null -> "Riot API request failed with status $status after $retries retries"
        else -> "Riot API request failed without a response after $retries retries: ${cause?.message}"
    }

    /** Releases the underlying HTTP engine's resources. */
    public fun close() {
        httpClient.close()
    }

    @PublishedApi
    internal sealed class RequestOutcome {
        class Ok(val response: HttpResponse, val retries: Int) : RequestOutcome()
        data object NotFound : RequestOutcome()
        class Failed(val failure: RiotResult.Failure) : RequestOutcome()
    }

    public companion object {
        public const val RIOT_KEY_HEADER: String = "X-Riot-Token"

        private const val HTTP_NO_CONTENT = 204
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private val SUCCESS_RANGE = 200..299
        private val SERVER_ERROR_RANGE = 500..599
        private const val MS_PER_SECOND = 1000L
        private const val INITIAL_BACKOFF_MS = 500L

        internal fun defaultHttpClient(): HttpClient = HttpClient {
            expectSuccess = false
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
        }
    }
}
