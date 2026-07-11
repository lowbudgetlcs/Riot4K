package com.lowbudgetlcs.riot4k.api.integration

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Live-API tests, skipped unless a `RIOT_API_KEY` environment variable is set.
 * Run on demand via the integration workflow, never as part of the regular build.
 *
 * `RIOT4K_TEST_RIOT_ID` (`gameName#tagLine`) overrides the account looked up.
 */
class AccountV1IntegrationTest {
    @Test
    fun resolvesARiotIdAgainstTheLiveApi() {
        val apiKey = System.getenv("RIOT_API_KEY")
        if (apiKey.isNullOrBlank()) {
            println("RIOT_API_KEY not set; skipping integration test")
            return
        }
        val riotId = System.getenv("RIOT4K_TEST_RIOT_ID") ?: "Hide on bush#KR1"
        val (gameName, tagLine) = riotId.split('#', limit = 2)

        val riot4k = Riot4K.create(apiKey)
        try {
            val result = runBlocking {
                riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)
            }
            val account = when (result) {
                is RiotResult.Success -> result.data
                else -> fail("expected $riotId to resolve, got $result")
            }
            assertTrue(account.puuid.isNotBlank(), "puuid should not be blank")
        } finally {
            riot4k.close()
        }
    }

    @Test
    fun unknownRiotIdReturnsNotFound() {
        val apiKey = System.getenv("RIOT_API_KEY")
        if (apiKey.isNullOrBlank()) {
            println("RIOT_API_KEY not set; skipping integration test")
            return
        }
        val riot4k = Riot4K.create(apiKey)
        try {
            val result = runBlocking {
                riot4k.accountV1().getByRiotId(
                    RegionalRoute.AMERICAS,
                    "riot4k-no-such-account",
                    "0000",
                )
            }
            assertIs<RiotResult.NotFound>(result, "expected NotFound, got $result")
        } finally {
            riot4k.close()
        }
    }
}
