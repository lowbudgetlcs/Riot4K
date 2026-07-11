package com.lowbudgetlcs.riot4k.samples.ios

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute

/**
 * Framework entry point consumed from Swift; see the sample's README for the
 * SwiftUI side. Suspend functions surface in Swift as async functions.
 */
class AccountSample(apiKey: String) {
    private val riot4k = Riot4K.create(apiKey)

    suspend fun describeAccount(gameName: String, tagLine: String): String =
        when (val result = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)) {
            is RiotResult.Success -> "$gameName#$tagLine -> puuid=${result.data.puuid}"
            is RiotResult.NotFound -> "No account with riot ID $gameName#$tagLine"
            is RiotResult.Failure ->
                "Request failed (status=${result.statusCode}, retries=${result.retries}): ${result.message}"
        }

    fun close() {
        riot4k.close()
    }
}
