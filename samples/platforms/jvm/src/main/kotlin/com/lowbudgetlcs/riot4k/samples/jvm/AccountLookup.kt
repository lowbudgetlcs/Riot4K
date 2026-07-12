package com.lowbudgetlcs.riot4k.samples.jvm

/** Application-level outcome of an account lookup. */
sealed interface AccountLookup {
    data class Found(val puuid: String, val riotId: String) : AccountLookup
    data object Missing : AccountLookup
    data class Error(val message: String) : AccountLookup
}
