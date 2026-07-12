@file:OptIn(ExperimentalJsExport::class)

package com.lowbudgetlcs.riot4k.api.js

import com.lowbudgetlcs.riot4k.api.endpoints.AccountV1
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

/**
 * account-v1 endpoints for JavaScript consumers. Obtain via [Riot4KJs.accountV1].
 */
@JsExport
public class AccountV1Js internal constructor(
    private val scope: CoroutineScope,
    private val delegate: AccountV1,
) {
    /**
     * Gets an account by its riot ID (`gameName#tagLine`).
     *
     * [route] is a regional routing value: `"AMERICAS"`, `"ASIA"`, `"EUROPE"`,
     * `"SEA"`, or `"ESPORTS"` (case-insensitive). An unknown route rejects the
     * promise; API outcomes (including errors) resolve to an [AccountResultJs].
     */
    public fun getByRiotId(route: String, gameName: String, tagLine: String): Promise<AccountResultJs> =
        scope.promise {
            delegate.getByRiotId(RegionalRoute.valueOf(route.uppercase()), gameName, tagLine).toJs()
        }
}
