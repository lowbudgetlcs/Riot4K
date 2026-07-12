package com.lowbudgetlcs.riot4k.samples.js

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute

/**
 * Looks up a riot ID and prints the account. Runs under Node.js.
 *
 * Usage: RIOT_API_KEY=RGAPI-... ./gradlew -p samples :js-sample:jsNodeRun
 */
suspend fun main() {
    val rawKey: dynamic = js("process.env.RIOT_API_KEY")
    val apiKey = (rawKey as? String)?.takeIf { it.isNotEmpty() }
        ?: error("Set the RIOT_API_KEY environment variable to your Riot API key")
    val gameName = "Hide on bush"
    val tagLine = "KR1"

    val riot4k = Riot4K.create(apiKey)
    val repository = AccountRepository(riot4k)
    try {
        when (val lookup = repository.lookup(RegionalRoute.AMERICAS, gameName, tagLine)) {
            is AccountLookup.Found -> println("${lookup.riotId} -> puuid=${lookup.puuid}")
            is AccountLookup.Missing -> println("No account with riot ID $gameName#$tagLine")
            is AccountLookup.Error -> println("Lookup failed: ${lookup.message}")
        }
    } finally {
        riot4k.close()
    }
}
