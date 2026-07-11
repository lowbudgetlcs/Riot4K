package com.lowbudgetlcs.riot4k.samples.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.lowbudgetlcs.riot4k.api.Riot4K
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Looks up a riot ID on launch and shows the result.
 *
 * The API key is injected at build time from the RIOT_API_KEY environment
 * variable (see build.gradle.kts); rebuild with it set to try the sample.
 */
class MainActivity : Activity() {
    private val scope = MainScope()
    private var riot4k: Riot4K? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this)
        setContentView(view)

        if (BuildConfig.RIOT_API_KEY.isEmpty()) {
            view.text = "Rebuild with the RIOT_API_KEY environment variable set"
            return
        }

        val client = Riot4K.create(BuildConfig.RIOT_API_KEY)
        riot4k = client
        val gameName = "Hide on bush"
        val tagLine = "KR1"

        view.text = "Looking up $gameName#$tagLine..."
        scope.launch {
            view.text = when (val result =
                client.accountV1().getByRiotId(RegionalRoute.AMERICAS, gameName, tagLine)) {
                is RiotResult.Success -> "$gameName#$tagLine\npuuid: ${result.data.puuid}"
                is RiotResult.NotFound -> "No account with riot ID $gameName#$tagLine"
                is RiotResult.Failure -> "Request failed (status=${result.statusCode}): ${result.message}"
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        riot4k?.close()
        super.onDestroy()
    }
}
