package com.lowbudgetlcs.riot4k.samples.android

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import com.lowbudgetlcs.riot4k.mockserver.MockRiotServer
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Contract test: the real repository over real HTTP against the mock server. */
class RiotAccountRepositoryTest {
    private lateinit var server: MockRiotServer
    private lateinit var riot4k: Riot4K
    private lateinit var repository: RiotAccountRepository

    @BeforeTest
    fun setUp() {
        server = MockRiotServer(Path.of(System.getProperty("fixturesDir")))
        val port = server.start()
        riot4k = Riot4K(
            Riot4KConfig.Builder("test-key")
                .baseUrlTemplate("http://127.0.0.1:$port")
                .maxRetries(0)
                .build(),
        )
        repository = RiotAccountRepository(riot4k)
    }

    @AfterTest
    fun tearDown() {
        riot4k.close()
        server.close()
    }

    @Test
    fun successfulLookupMapsToFound() = runBlocking<Unit> {
        val lookup = repository.lookup(RegionalRoute.AMERICAS, "Hide on bush", "KR1")
        val found = assertIs<AccountLookup.Found>(lookup)
        assertEquals("Hide on bush#KR1", found.riotId)
    }

    @Test
    fun unknownRiotIdMapsToMissing() = runBlocking<Unit> {
        assertIs<AccountLookup.Missing>(repository.lookup(RegionalRoute.AMERICAS, "NotFound", "x"))
    }
}
