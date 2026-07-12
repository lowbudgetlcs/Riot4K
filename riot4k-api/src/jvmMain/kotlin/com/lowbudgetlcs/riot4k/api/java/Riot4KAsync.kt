package com.lowbudgetlcs.riot4k.api.java

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Entry point of the SDK for Java consumers: every endpoint method returns a
 * [java.util.concurrent.CompletableFuture] carrying a
 * [com.lowbudgetlcs.riot4k.core.result.RiotResult], so API errors stay typed
 * values rather than exceptions. Pattern-match the result:
 *
 * ```java
 * try (var riot4k = Riot4KAsync.create(apiKey)) {
 *     var result = riot4k.accountV1()
 *         .getByRiotIdAsync(RegionalRoute.AMERICAS, "gameName", "tagLine")
 *         .join();
 *     if (result instanceof RiotResult.Success<AccountDto> success) {
 *         System.out.println(success.getData().getPuuid());
 *     } else if (result instanceof RiotResult.NotFound) {
 *         System.out.println("No such riot ID");
 *     } else if (result instanceof RiotResult.Failure failure) {
 *         System.out.println("Failed with status " + failure.getStatusCode());
 *     }
 * }
 * ```
 *
 * Create one instance per API key and share it; [close] cancels in-flight
 * requests and releases the HTTP engine.
 */
public class Riot4KAsync(private val riot4k: Riot4K) : AutoCloseable {
    public constructor(config: Riot4KConfig) : this(Riot4K(config))

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val accountV1 = AccountV1Async(scope, riot4k.accountV1())

    /** account-v1: riot accounts and riot IDs. */
    public fun accountV1(): AccountV1Async = accountV1

    override fun close() {
        scope.cancel()
        riot4k.close()
    }

    public companion object {
        /** Creates a client with default configuration for [apiKey]. */
        @JvmStatic
        public fun create(apiKey: String): Riot4KAsync = Riot4KAsync(Riot4KConfig.of(apiKey))
    }
}
