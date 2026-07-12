package com.lowbudgetlcs.riot4k.samples.jvm

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.runBlocking

/**
 * Looks up a riot ID and prints the account.
 *
 * Usage: RIOT_API_KEY=RGAPI-... ./gradlew -p samples :jvm-sample:run --args="gameName tagLine"
 */
fun main(args: Array<String>) {
    val apiKey = System.getenv("RIOT_API_KEY")
        ?: error("Set the RIOT_API_KEY environment variable to your Riot API key")
    val gameName = args.getOrElse(0) { "Hide on bush" }
    val tagLine = args.getOrElse(1) { "KR1" }

    val riot4k = Riot4K.create(apiKey)
    val repository = AccountRepository(riot4k)
    try {
        runBlocking {
            when (val lookup = repository.lookup(RegionalRoute.AMERICAS, gameName, tagLine)) {
                is AccountLookup.Found -> println("${lookup.riotId} -> puuid=${lookup.puuid}")
                is AccountLookup.Missing -> println("No account with riot ID $gameName#$tagLine")
                is AccountLookup.Error -> println("Lookup failed: ${lookup.message}")
            }
        }
    } finally {
        riot4k.close()
    }
}
