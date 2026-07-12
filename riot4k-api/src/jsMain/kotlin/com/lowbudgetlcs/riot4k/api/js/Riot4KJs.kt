@file:OptIn(ExperimentalJsExport::class)

package com.lowbudgetlcs.riot4k.api.js

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Entry point of the SDK for JavaScript/TypeScript consumers. Create one
 * instance per API key and share it; call [close] when done to release the
 * HTTP engine and cancel in-flight requests.
 *
 * ```ts
 * const riot4k = new Riot4KJs(apiKey);
 * const result = await riot4k.accountV1().getByRiotId("AMERICAS", "gameName", "tagLine");
 * if (result.type === "success") console.log(result.account?.puuid);
 * ```
 */
@JsExport
public class Riot4KJs(
    apiKey: String,
    baseUrlTemplate: String? = null,
    maxRetries: Int? = null,
) {
    private val riot4k: Riot4K = Riot4K(
        Riot4KConfig.Builder(apiKey)
            .apply {
                baseUrlTemplate?.let { baseUrlTemplate(it) }
                maxRetries?.let { maxRetries(it) }
            }
            .build(),
    )
    private val scope = CoroutineScope(SupervisorJob())
    private val accountV1 = AccountV1Js(scope, riot4k.accountV1())

    /** account-v1: riot accounts and riot IDs. */
    public fun accountV1(): AccountV1Js = accountV1

    /** Cancels in-flight requests and releases the underlying HTTP engine. */
    public fun close() {
        scope.cancel()
        riot4k.close()
    }
}
