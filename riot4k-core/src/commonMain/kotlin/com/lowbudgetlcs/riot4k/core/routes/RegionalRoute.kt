package com.lowbudgetlcs.riot4k.core.routes

/**
 * Regional routing values, used by account-v1, match-v5, LoR, Riftbound content,
 * and tournament endpoints.
 */
public enum class RegionalRoute(override val subdomain: String) : Route {
    AMERICAS("americas"),
    ASIA("asia"),
    EUROPE("europe"),
    SEA("sea"),

    /** Special routing value; usable only with account-v1 on esports-scoped API keys. */
    ESPORTS("esports"),
}
