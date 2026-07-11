package com.lowbudgetlcs.riot4k.api

import com.lowbudgetlcs.riot4k.api.endpoints.AccountV1
import com.lowbudgetlcs.riot4k.core.RiotHttpClient
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig

/**
 * Entry point of the SDK. Create one instance per API key and share it: it owns
 * the HTTP client and the rate limiter, both of which must be shared for rate
 * limits to be enforced correctly across concurrent calls.
 *
 * ```kotlin
 * val riot4k = Riot4K.create(apiKey)
 * when (val account = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, "gameName", "tagLine")) {
 *     is RiotResult.Success -> println(account.data.puuid)
 *     is RiotResult.NotFound -> println("No such riot ID")
 *     is RiotResult.Failure -> println("Request failed: ${account.message}")
 * }
 * ```
 */
public class Riot4K(private val client: RiotHttpClient) {
    public constructor(config: Riot4KConfig) : this(RiotHttpClient(config))

    private val accountV1: AccountV1 = AccountV1(client)

    /** account-v1: riot accounts and riot IDs. */
    public fun accountV1(): AccountV1 = accountV1

    /** Releases the underlying HTTP engine's resources. */
    public fun close() {
        client.close()
    }

    public companion object {
        /** Creates a client with default configuration for [apiKey]. */
        public fun create(apiKey: String): Riot4K = Riot4K(Riot4KConfig.of(apiKey))
    }
}
