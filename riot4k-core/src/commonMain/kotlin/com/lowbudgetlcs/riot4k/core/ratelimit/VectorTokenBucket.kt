package com.lowbudgetlcs.riot4k.core.ratelimit

import com.lowbudgetlcs.riot4k.core.models.RateLimitConfig
import kotlin.math.ceil

/**
 * Sliding-window token bucket.
 *
 * Tracks the issue timestamp of each token inside the window. Two limits apply:
 * the full [RateLimitConfig.limit] per window, and a burst limit
 * ([RateLimitConfig.effectiveLimit] per `window * burstFactor`) that spreads
 * spend across the window when `burstFactor < 1`. [durationOverheadMs] pads the
 * window to absorb clock skew between us and Riot's edge.
 *
 * NOT thread-safe: callers ([HeaderRateLimiter]) must serialize access.
 */
public class VectorTokenBucket(
    private val config: RateLimitConfig,
    private val durationOverheadMs: Long = 0,
) {
    init {
        require(durationOverheadMs >= 0) { "durationOverheadMs must be >= 0, was $durationOverheadMs" }
    }

    private val timestamps = ArrayDeque<Long>() // newest first

    private val windowMs: Long = config.windowMs + durationOverheadMs
    private val burstWindowMs: Long = ceil(config.windowMs * config.burstFactor).toLong() + durationOverheadMs
    private val burstLimit: Int = config.effectiveLimit

    /**
     * Milliseconds until a token becomes available, or `0` if one is available now.
     */
    public fun getDelay(nowMs: Long): Long {
        prune(nowMs)
        // Full window limit.
        timestamps.getOrNull(config.limit - 1)?.let { nth ->
            val readyAt = nth + windowMs
            if (readyAt > nowMs) return readyAt - nowMs
        }
        // Burst limit.
        timestamps.getOrNull(burstLimit - 1)?.let { nth ->
            val readyAt = nth + burstWindowMs
            if (readyAt > nowMs) return readyAt - nowMs
        }
        return 0
    }

    /** Records [n] tokens issued at [nowMs]. */
    public fun getTokens(nowMs: Long, n: Int = 1) {
        repeat(n) { timestamps.addFirst(nowMs) }
    }

    /** Fraction of the full limit currently free: `1.0` empty … `0.0` exhausted. */
    public fun getCapacity(nowMs: Long): Double {
        prune(nowMs)
        return (config.limit - timestamps.size).coerceAtLeast(0).toDouble() / config.limit
    }

    private fun prune(nowMs: Long) {
        while (timestamps.isNotEmpty() && timestamps.last() + windowMs <= nowMs) {
            timestamps.removeLast()
        }
    }
}
