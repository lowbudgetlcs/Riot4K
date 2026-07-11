package com.lowbudgetlcs.riot4k.core.models

import kotlin.math.floor

/**
 * Parameters of a single rate-limit bucket: [limit] requests per [windowMs].
 *
 * [burstFactor] (0, 1] controls how much of the limit may be spent in the same
 * fraction of the window: a high factor allows near-instant bursts (low latency),
 * a low factor spreads requests evenly (fewer 429s under sustained load).
 */
public data class RateLimitConfig(
    val limit: Int,
    val windowMs: Long,
    val burstFactor: Double = 1.0,
) {
    init {
        require(limit > 0) { "limit must be > 0, was $limit" }
        require(windowMs > 0) { "windowMs must be > 0, was $windowMs" }
        require(burstFactor > 0.0 && burstFactor <= 1.0) { "burstFactor must be in (0, 1], was $burstFactor" }
    }

    /** Requests allowed inside the burst fraction of the window; always at least 1. */
    val effectiveLimit: Int = floor(limit * burstFactor).toInt().coerceAtLeast(1)
}
