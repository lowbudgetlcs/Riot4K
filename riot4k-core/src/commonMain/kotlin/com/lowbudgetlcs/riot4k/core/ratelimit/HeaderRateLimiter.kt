package com.lowbudgetlcs.riot4k.core.ratelimit

import com.lowbudgetlcs.riot4k.core.models.Clock
import com.lowbudgetlcs.riot4k.core.models.RateLimitConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Full header-driven rate limiter enforcing Riot's dual-tier limits:
 *
 * - an **app tier** per route (from `X-App-Rate-Limit`), shared by all methods, and
 * - a **method tier** per route+method (from `X-Method-Rate-Limit`),
 *
 * each holding one [VectorTokenBucket] per `count:seconds` entry. Until the first
 * response is seen, a conservative 1-request/second seed bucket throttles the cold
 * start so real limits are learned before traffic ramps. Buckets are rebuilt when
 * the advertised limits change. A 429 is attributed via `X-Rate-Limit-Type`
 * (`application` penalizes the app tier; `method`/`service` the method tier) and
 * blocks that tier for `Retry-After` seconds.
 */
public class HeaderRateLimiter(
    private val clock: Clock,
    private val burstFactor: Double = 1.0,
    private val durationOverheadMs: Long = 0,
) : RateLimiter {
    private val mutex = Mutex()
    private val appTiers = mutableMapOf<String, Tier>()
    private val methodTiers = mutableMapOf<String, Tier>()

    override suspend fun acquire(routeKey: String, methodKey: String) {
        while (true) {
            val waitMs = mutex.withLock {
                val now = clock.nowMillis()
                val app = appTiers.getOrPut(routeKey) { Tier() }
                val method = methodTiers.getOrPut("$routeKey/$methodKey") { Tier() }
                val delayMs = maxOf(app.getDelay(now), method.getDelay(now))
                if (delayMs <= 0) {
                    app.getTokens(now)
                    method.getTokens(now)
                }
                delayMs
            }
            if (waitMs <= 0) return
            delay(waitMs)
        }
    }

    override suspend fun onResponse(
        routeKey: String,
        methodKey: String,
        statusCode: Int,
        headers: (String) -> String?,
    ) {
        mutex.withLock {
            val now = clock.nowMillis()
            val app = appTiers.getOrPut(routeKey) { Tier() }
            val method = methodTiers.getOrPut("$routeKey/$methodKey") { Tier() }
            app.syncLimits(headers(RateLimitHeaders.APP_RATE_LIMIT))
            method.syncLimits(headers(RateLimitHeaders.METHOD_RATE_LIMIT))

            if (statusCode == STATUS_TOO_MANY_REQUESTS) {
                val retryAfterMs = headers(RateLimitHeaders.RETRY_AFTER)?.trim()?.toLongOrNull()
                    ?.times(MS_PER_SECOND) ?: DEFAULT_RETRY_AFTER_MS
                when (headers(RateLimitHeaders.RATE_LIMIT_TYPE)?.trim()?.lowercase()) {
                    "application" -> app.penalize(now + retryAfterMs)
                    // "method", "service", and missing all penalize the method tier.
                    else -> method.penalize(now + retryAfterMs)
                }
            }
        }
    }

    private inner class Tier {
        /** Raw header value the buckets were built from; null = still on the seed bucket. */
        private var limitsHeader: String? = null
        private var buckets: List<VectorTokenBucket> = listOf(seedBucket())
        private var blockedUntilMs: Long = 0

        fun getDelay(nowMs: Long): Long {
            val penalty = (blockedUntilMs - nowMs).coerceAtLeast(0)
            val bucketDelay = buckets.maxOf { it.getDelay(nowMs) }
            return maxOf(penalty, bucketDelay)
        }

        fun getTokens(nowMs: Long) {
            buckets.forEach { it.getTokens(nowMs) }
        }

        fun syncLimits(header: String?) {
            if (header == null || header == limitsHeader) return
            val entries = RateLimitHeaders.parse(header)
            if (entries.isEmpty()) return
            limitsHeader = header
            buckets = entries.map { entry ->
                VectorTokenBucket(
                    RateLimitConfig(
                        limit = entry.count,
                        windowMs = entry.windowSeconds * MS_PER_SECOND,
                        burstFactor = burstFactor,
                    ),
                    durationOverheadMs = durationOverheadMs,
                )
            }
        }

        fun penalize(untilMs: Long) {
            if (untilMs > blockedUntilMs) blockedUntilMs = untilMs
        }
    }

    private fun seedBucket() = VectorTokenBucket(
        RateLimitConfig(limit = 1, windowMs = MS_PER_SECOND),
        durationOverheadMs = durationOverheadMs,
    )

    private companion object {
        const val STATUS_TOO_MANY_REQUESTS = 429
        const val MS_PER_SECOND = 1000L
        const val DEFAULT_RETRY_AFTER_MS = 1000L
    }
}
