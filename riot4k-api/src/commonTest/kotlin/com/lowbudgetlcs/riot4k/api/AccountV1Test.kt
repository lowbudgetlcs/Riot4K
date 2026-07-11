package com.lowbudgetlcs.riot4k.api

import com.lowbudgetlcs.riot4k.core.RiotHttpClient
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import com.lowbudgetlcs.riot4k.core.ratelimit.RateLimiter
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import com.lowbudgetlcs.riot4k.models.account.v1.AccountDto
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private object NoopRateLimiter : RateLimiter {
    override suspend fun acquire(routeKey: String, methodKey: String) = Unit
    override suspend fun onResponse(
        routeKey: String,
        methodKey: String,
        statusCode: Int,
        headers: (String) -> String?,
    ) = Unit
}

class AccountV1Test {
    private fun riot4k(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Riot4K {
        val http = HttpClient(MockEngine(handler)) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return Riot4K(RiotHttpClient(Riot4KConfig.of("test-key"), NoopRateLimiter, http))
    }

    @Test
    fun getByRiotIdBuildsTheCorrectUrl() = runTest {
        val riot4k = riot4k { request ->
            assertEquals(
                "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/Game%20Name/NA1",
                request.url.toString(),
            )
            respond(
                """{"puuid":"p-123","gameName":"Game Name","tagLine":"NA1"}""",
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"),
            )
        }
        val result = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, "Game Name", "NA1")
        assertEquals(
            RiotResult.Success(AccountDto("p-123", "Game Name", "NA1")),
            result,
        )
    }

    @Test
    fun unknownRiotIdIsNotFound() = runTest {
        val riot4k = riot4k {
            respond(
                """{"status":{"message":"Data not found","status_code":404}}""",
                HttpStatusCode.NotFound,
                headersOf("Content-Type", "application/json"),
            )
        }
        val result = riot4k.accountV1().getByRiotId(RegionalRoute.EUROPE, "nobody", "0000")
        assertEquals(RiotResult.NotFound, result)
    }

    @Test
    fun absentOptionalFieldsDecode() = runTest {
        val riot4k = riot4k {
            respond(
                """{"puuid":"p-123"}""",
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"),
            )
        }
        val result = riot4k.accountV1().getByRiotId(RegionalRoute.ASIA, "name", "tag")
        assertEquals(RiotResult.Success(AccountDto("p-123")), result)
    }
}
