package com.lowbudgetlcs.riot4k.core.routes

/**
 * A Riot API routing value; [subdomain] is substituted into the base URL template
 * (`https://{route}.api.riotgames.com`).
 */
public interface Route {
    public val subdomain: String
}
