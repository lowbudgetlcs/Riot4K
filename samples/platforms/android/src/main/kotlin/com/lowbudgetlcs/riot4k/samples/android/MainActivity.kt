package com.lowbudgetlcs.riot4k.samples.android

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lowbudgetlcs.riot4k.api.Riot4K
import kotlinx.coroutines.launch

/**
 * Looks up a riot ID on launch and renders the ViewModel's state.
 *
 * The API key is injected at build time from the RIOT_API_KEY environment
 * variable (see build.gradle.kts); rebuild with it set to try the sample.
 */
class MainActivity : ComponentActivity() {
    private var riot4k: Riot4K? = null

    private val viewModel: AccountViewModel by viewModels {
        AccountViewModel.factory {
            RiotAccountRepository(
                Riot4K.create(BuildConfig.RIOT_API_KEY).also { riot4k = it },
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this)
        setContentView(view)

        if (BuildConfig.RIOT_API_KEY.isEmpty()) {
            view.text = "Rebuild with the RIOT_API_KEY environment variable set"
            return
        }

        lifecycleScope.launch {
            viewModel.state.collect { state -> view.text = state.render() }
        }
        viewModel.load("Hide on bush", "KR1")
    }

    override fun onDestroy() {
        riot4k?.close()
        super.onDestroy()
    }

    private fun AccountUiState.render(): String = when (this) {
        is AccountUiState.Idle -> ""
        is AccountUiState.Loading -> "Looking up $riotId..."
        is AccountUiState.Result -> when (val lookup = lookup) {
            is AccountLookup.Found -> "${lookup.riotId}\npuuid: ${lookup.puuid}"
            is AccountLookup.Missing -> "No account with that riot ID"
            is AccountLookup.Error -> "Lookup failed: ${lookup.message}"
        }
    }
}
