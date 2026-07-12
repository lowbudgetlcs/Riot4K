package com.lowbudgetlcs.riot4k.samples.android

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute

/** Application-level outcome of an account lookup. */
sealed interface AccountLookup {
    data class Found(val puuid: String, val riotId: String) : AccountLookup
    data object Missing : AccountLookup
    data class Error(val message: String) : AccountLookup
}

/** The app's account access layer; the interface keeps the ViewModel testable. */
interface AccountRepository {
    suspend fun lookup(route: RegionalRoute, gameName: String, tagLine: String): AccountLookup
}

class RiotAccountRepository(private val riot4k: Riot4K) : AccountRepository {
    override suspend fun lookup(route: RegionalRoute, gameName: String, tagLine: String): AccountLookup =
        when (val result = riot4k.accountV1().getByRiotId(route, gameName, tagLine)) {
            is RiotResult.Success -> AccountLookup.Found(
                puuid = result.data.puuid,
                riotId = "${result.data.gameName ?: gameName}#${result.data.tagLine ?: tagLine}",
            )
            is RiotResult.NotFound -> AccountLookup.Missing
            is RiotResult.Failure -> AccountLookup.Error(result.message)
        }
}
