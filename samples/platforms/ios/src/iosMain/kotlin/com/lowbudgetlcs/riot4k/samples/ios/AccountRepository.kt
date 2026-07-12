package com.lowbudgetlcs.riot4k.samples.ios

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.config.Riot4KConfig
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute

/**
 * Application-level outcome of an account lookup, as plain classes so Swift
 * consumers can switch over the hierarchy.
 */
sealed interface AccountLookup {
    data class Found(val puuid: String, val riotId: String) : AccountLookup
    data object Missing : AccountLookup
    data class Error(val message: String) : AccountLookup
}

/**
 * The application's account access layer, consumed from Swift; suspend
 * functions surface in Swift as async functions.
 */
class AccountRepository(apiKey: String, baseUrlTemplate: String? = null) {
    private val riot4k = Riot4K(
        Riot4KConfig.Builder(apiKey)
            .apply { baseUrlTemplate?.let { baseUrlTemplate(it) } }
            .build(),
    )

    suspend fun lookup(gameName: String, tagLine: String): AccountLookup =
        when (val result = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)) {
            is RiotResult.Success -> AccountLookup.Found(
                puuid = result.data.puuid,
                riotId = "${result.data.gameName ?: gameName}#${result.data.tagLine ?: tagLine}",
            )
            is RiotResult.NotFound -> AccountLookup.Missing
            is RiotResult.Failure -> AccountLookup.Error(result.message)
        }

    fun close() {
        riot4k.close()
    }
}
