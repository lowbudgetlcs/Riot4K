package com.lowbudgetlcs.riot4k.samples.linux

import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix.getenv

/**
 * Looks up a riot ID and prints the account.
 *
 * Usage: RIOT_API_KEY=RGAPI-... ./linux-sample.kexe [gameName] [tagLine]
 */
@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val apiKey = getenv("RIOT_API_KEY")?.toKString()
        ?: error("Set the RIOT_API_KEY environment variable to your Riot API key")
    val gameName = args.getOrElse(0) { "Hide on bush" }
    val tagLine = args.getOrElse(1) { "KR1" }

    val riot4k = Riot4K.create(apiKey)
    try {
        runBlocking {
            when (val result = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)) {
                is RiotResult.Success ->
                    println("$gameName#$tagLine -> puuid=${result.data.puuid}")
                is RiotResult.NotFound ->
                    println("No account with riot ID $gameName#$tagLine")
                is RiotResult.Failure ->
                    println("Request failed (status=${result.statusCode}, retries=${result.retries}): ${result.message}")
            }
        }
    } finally {
        riot4k.close()
    }
}
