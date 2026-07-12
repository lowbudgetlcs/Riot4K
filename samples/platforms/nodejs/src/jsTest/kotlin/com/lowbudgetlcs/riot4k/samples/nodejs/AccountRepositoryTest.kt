package com.lowbudgetlcs.riot4k.samples.nodejs

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.RiotHttpClient
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Verifies the sample's result-to-state mapping. The Node-runtime localhost
 * contract run against the shared mock server lives in the TypeScript language sample suite.
 */
class AccountRepositoryTest {
    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): AccountRepository {
        val http = HttpClient(MockEngine(handler)) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return AccountRepository(Riot4K(RiotHttpClient(Riot4KConfig.of("test-key"), httpClient = http)))
    }

    @Test
    fun successfulLookupMapsToFound() = runTest {
        val repository = repository {
            respond(
                """{"puuid":"p-1","gameName":"Hide on bush","tagLine":"KR1"}""",
                HttpStatusCode.OK,
                headersOf("Content-Type", "application/json"),
            )
        }
        val found = assertIs<AccountLookup.Found>(
            repository.lookup(RegionalRoute.AMERICAS, "Hide on bush", "KR1"),
        )
        assertEquals("Hide on bush#KR1", found.riotId)
        assertEquals("p-1", found.puuid)
    }

    @Test
    fun unknownRiotIdMapsToMissing() = runTest {
        val repository = repository { respond("", HttpStatusCode.NotFound) }
        assertIs<AccountLookup.Missing>(repository.lookup(RegionalRoute.AMERICAS, "nobody", "x"))
    }

    @Test
    fun apiFailureMapsToError() = runTest {
        val repository = repository { respond("oops", HttpStatusCode.Forbidden) }
        val error = assertIs<AccountLookup.Error>(repository.lookup(RegionalRoute.AMERICAS, "x", "y"))
        assertEquals(true, error.message.contains("403"))
    }
}
