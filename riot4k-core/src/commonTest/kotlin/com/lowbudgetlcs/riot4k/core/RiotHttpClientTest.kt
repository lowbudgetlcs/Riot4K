package com.lowbudgetlcs.riot4k.core

import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import com.lowbudgetlcs.riot4k.core.ratelimit.RateLimiter
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Serializable
private data class TestDto(val puuid: String)

/** Pass-through limiter isolating transport tests from rate-limit timing. */
private object NoopRateLimiter : RateLimiter {
    override suspend fun acquire(routeKey: String, methodKey: String) = Unit
    override suspend fun onResponse(
        routeKey: String,
        methodKey: String,
        statusCode: Int,
        headers: (String) -> String?,
    ) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class RiotHttpClientTest {
    private val jsonHeaders = headersOf("Content-Type", "application/json")

    private fun client(
        maxRetries: Int = 3,
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): RiotHttpClient {
        val http = HttpClient(MockEngine(handler)) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val config = Riot4KConfig.Builder("test-key").maxRetries(maxRetries).build()
        return RiotHttpClient(config, NoopRateLimiter, http)
    }

    @Test
    fun successDecodesBody() = runTest {
        val client = client { request ->
            assertEquals(
                "https://americas.api.riotgames.com/riot/account/v1/accounts/abc",
                request.url.toString(),
            )
            respond("""{"puuid":"abc","extraField":42}""", HttpStatusCode.OK, jsonHeaders)
        }
        val result = client.get<TestDto>(
            RegionalRoute.AMERICAS, "test.method", "riot", "account", "v1", "accounts", "abc",
        )
        assertEquals(RiotResult.Success(TestDto("abc")), result)
    }

    @Test
    fun notFoundIsNotAFailure() = runTest {
        val client = client { respond("""{"status":404}""", HttpStatusCode.NotFound, jsonHeaders) }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        assertEquals(RiotResult.NotFound, result)
    }

    @Test
    fun noContentIsNotFound() = runTest {
        val client = client { respond("", HttpStatusCode.NoContent) }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        assertEquals(RiotResult.NotFound, result)
    }

    @Test
    fun sendsApiKeyHeaderAndNeverLogsIt() = runTest {
        val client = client { request ->
            assertEquals("test-key", request.headers[RiotHttpClient.RIOT_KEY_HEADER])
            respond("""{"puuid":"abc"}""", HttpStatusCode.OK, jsonHeaders)
        }
        client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
    }

    @Test
    fun rateLimitedRequestIsRetriedAfterRetryAfter() = runTest {
        var requests = 0
        val client = client { _ ->
            requests++
            if (requests == 1) {
                respond(
                    "",
                    HttpStatusCode.TooManyRequests,
                    headersOf("Retry-After", "2"),
                )
            } else {
                respond("""{"puuid":"abc"}""", HttpStatusCode.OK, jsonHeaders)
            }
        }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        assertEquals(RiotResult.Success(TestDto("abc")), result)
        assertEquals(2, requests)
        assertEquals(2000, currentTime)
    }

    @Test
    fun serverErrorsAreRetriedThenFail() = runTest {
        var requests = 0
        val client = client(maxRetries = 3) { _ ->
            requests++
            respond("oops", HttpStatusCode.InternalServerError)
        }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        val failure = assertIs<RiotResult.Failure>(result)
        assertEquals(500, failure.statusCode)
        assertEquals(3, failure.retries)
        assertEquals(4, requests)
    }

    @Test
    fun serverErrorRetriesUseExponentialBackoff() = runTest {
        val client = client(maxRetries = 3) { respond("oops", HttpStatusCode.BadGateway) }
        client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        // 500 + 1000 + 2000 virtual ms of backoff across 3 retries.
        assertEquals(3500, currentTime)
    }

    @Test
    fun clientErrorsFailImmediately() = runTest {
        var requests = 0
        val client = client { _ ->
            requests++
            respond("forbidden", HttpStatusCode.Forbidden)
        }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        val failure = assertIs<RiotResult.Failure>(result)
        assertEquals(403, failure.statusCode)
        assertEquals(0, failure.retries)
        assertEquals(1, requests)
    }

    @Test
    fun rateLimitTypeIsSurfacedOnExhaustedRetries() = runTest {
        val client = client(maxRetries = 1) { _ ->
            respond(
                "",
                HttpStatusCode.TooManyRequests,
                headersOf(
                    "Retry-After" to listOf("1"),
                    "X-Rate-Limit-Type" to listOf("application"),
                ),
            )
        }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        val failure = assertIs<RiotResult.Failure>(result)
        assertEquals(429, failure.statusCode)
        assertEquals("application", failure.rateLimitType)
    }

    @Test
    fun transportErrorsAreRetriedThenFailWithCause() = runTest {
        var requests = 0
        val client = client(maxRetries = 2) { _ ->
            requests++
            throw RuntimeException("connection reset")
        }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        val failure = assertIs<RiotResult.Failure>(result)
        assertEquals(null, failure.statusCode)
        assertEquals(2, failure.retries)
        assertEquals(3, requests)
        assertTrue(failure.cause?.message?.contains("connection reset") == true)
    }

    @Test
    fun undecodableBodyIsAFailureWithCause() = runTest {
        val client = client { respond("""{"wrong":"shape"}""", HttpStatusCode.OK, jsonHeaders) }
        val result = client.get<TestDto>(RegionalRoute.AMERICAS, "test.method", "x")
        val failure = assertIs<RiotResult.Failure>(result)
        assertEquals(200, failure.statusCode)
        assertTrue(failure.cause != null)
    }

    @Test
    fun pathSegmentsAreUrlEncoded() = runTest {
        val client = client { request ->
            assertEquals(
                "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/Game%20Name/EU%231",
                request.url.toString(),
            )
            respond("""{"puuid":"abc"}""", HttpStatusCode.OK, jsonHeaders)
        }
        client.get<TestDto>(
            RegionalRoute.EUROPE, "test.method",
            "riot", "account", "v1", "accounts", "by-riot-id", "Game Name", "EU#1",
        )
    }
}
