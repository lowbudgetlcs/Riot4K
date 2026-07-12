package com.lowbudgetlcs.riot4k.api.java

import com.lowbudgetlcs.riot4k.api.endpoints.AccountV1
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import com.lowbudgetlcs.riot4k.models.account.v1.AccountDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

/**
 * account-v1 endpoints for Java consumers. Obtain via [Riot4KAsync.accountV1].
 */
public class AccountV1Async internal constructor(
    private val scope: CoroutineScope,
    private val delegate: AccountV1,
) {
    /**
     * Gets an account by its riot ID (`gameName#tagLine`).
     *
     * The future completes with [RiotResult.NotFound] when no account has that
     * riot ID and [RiotResult.Failure] on API errors; it completes exceptionally
     * only if cancelled.
     */
    public fun getByRiotIdAsync(
        route: RegionalRoute,
        gameName: String,
        tagLine: String,
    ): CompletableFuture<RiotResult<AccountDto>> =
        scope.future { delegate.getByRiotId(route, gameName, tagLine) }
}
