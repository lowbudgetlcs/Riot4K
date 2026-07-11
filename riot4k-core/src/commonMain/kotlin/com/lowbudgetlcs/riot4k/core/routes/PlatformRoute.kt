package com.lowbudgetlcs.riot4k.core.routes

/**
 * Platform (shard) routing values, used by per-platform endpoints such as
 * summoner-v4 and league-v4. [toRegional] derives the regional cluster
 * serving each platform.
 */
public enum class PlatformRoute(
    override val subdomain: String,
    public val regional: RegionalRoute,
) : Route {
    BR1("br1", RegionalRoute.AMERICAS),
    EUN1("eun1", RegionalRoute.EUROPE),
    EUW1("euw1", RegionalRoute.EUROPE),
    JP1("jp1", RegionalRoute.ASIA),
    KR("kr", RegionalRoute.ASIA),
    LA1("la1", RegionalRoute.AMERICAS),
    LA2("la2", RegionalRoute.AMERICAS),
    ME1("me1", RegionalRoute.EUROPE),
    NA1("na1", RegionalRoute.AMERICAS),
    OC1("oc1", RegionalRoute.SEA),
    RU("ru", RegionalRoute.EUROPE),
    SG2("sg2", RegionalRoute.SEA),
    TR1("tr1", RegionalRoute.EUROPE),
    TW2("tw2", RegionalRoute.SEA),
    VN2("vn2", RegionalRoute.SEA),
    ;

    public fun toRegional(): RegionalRoute = regional
}
