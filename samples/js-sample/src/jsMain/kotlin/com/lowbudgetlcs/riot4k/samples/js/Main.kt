package com.lowbudgetlcs.riot4k.samples.js

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
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
    try {
        when (val result = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)) {
            is RiotResult.Success ->
                println("$gameName#$tagLine -> puuid=${result.data.puuid}")
            is RiotResult.NotFound ->
                println("No account with riot ID $gameName#$tagLine")
            is RiotResult.Failure ->
                println("Request failed (status=${result.statusCode}, retries=${result.retries}): ${result.message}")
        }
    } finally {
        riot4k.close()
    }
}
