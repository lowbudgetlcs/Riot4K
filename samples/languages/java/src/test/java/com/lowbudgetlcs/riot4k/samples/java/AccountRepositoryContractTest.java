package com.lowbudgetlcs.riot4k.samples.java;

import com.lowbudgetlcs.riot4k.api.Riot4K;
import com.lowbudgetlcs.riot4k.api.java.Riot4KAsync;
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig;
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute;
import com.lowbudgetlcs.riot4k.mockserver.MockRiotServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract tests: the Java binding exercised end-to-end over real HTTP against
 * the shared mock Riot server.
 */
class AccountRepositoryContractTest {
    private MockRiotServer server;
    private Riot4KAsync riot4k;
    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        server = new MockRiotServer(Path.of(System.getProperty("fixturesDir")));
        int port = server.start(0);
        riot4k = new Riot4KAsync(
            new Riot4K(
                new Riot4KConfig.Builder("test-key")
                    .baseUrlTemplate("http://127.0.0.1:" + port)
                    .maxRetries(1)
                    .build()));
        repository = new AccountRepository(riot4k);
    }

    @AfterEach
    void tearDown() {
        riot4k.close();
        server.close();
    }

    @Test
    void successfulLookupMapsToFoundWithEchoedRiotId() {
        AccountLookup lookup = repository.lookup(RegionalRoute.AMERICAS, "Hide on bush", "KR1").join();
        AccountLookup.Found found = assertInstanceOf(AccountLookup.Found.class, lookup);
        assertEquals("Hide on bush#KR1", found.riotId());
        assertTrue(found.puuid().startsWith("mock-puuid-"));
    }

    @Test
    void unknownRiotIdMapsToMissingNotError() {
        AccountLookup lookup = repository.lookup(RegionalRoute.AMERICAS, "NotFound", "x").join();
        assertInstanceOf(AccountLookup.Missing.class, lookup);
    }

    @Test
    void apiFailureMapsToErrorWithStatus() {
        AccountLookup lookup = repository.lookup(RegionalRoute.AMERICAS, "ServerError", "x").join();
        AccountLookup.Error error = assertInstanceOf(AccountLookup.Error.class, lookup);
        assertTrue(error.message().contains("500"));
    }

    @Test
    void rateLimitedRequestIsRetriedToSuccess() {
        // First call for this tagLine answers 429 with Retry-After: 1; the SDK
        // must honor it and succeed on the retry.
        AccountLookup lookup = repository.lookup(RegionalRoute.AMERICAS, "RateLimited", "retry-1").join();
        assertInstanceOf(AccountLookup.Found.class, lookup);
    }
}
