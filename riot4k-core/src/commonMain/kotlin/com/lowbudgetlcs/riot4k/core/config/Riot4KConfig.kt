package com.lowbudgetlcs.riot4k.core.config

import com.lowbudgetlcs.riot4k.core.models.Clock
import com.lowbudgetlcs.riot4k.core.models.SystemClock

/**
 * Client configuration.
 *
 * Built via [Builder] (fluent, usable from Java and future JS facades) or the
 * [of]/[burst]/[throughput] factories.
 */
public class Riot4KConfig private constructor(
    public val apiKey: String,
    public val maxRetries: Int,
    public val burstFactor: Double,
    public val durationOverheadMs: Long,
    public val baseUrlTemplate: String,
    public val clock: Clock,
) {
    public class Builder(private val apiKey: String) {
        private var maxRetries: Int = DEFAULT_MAX_RETRIES
        private var burstFactor: Double = BURST_BURST_FACTOR
        private var durationOverheadMs: Long = BURST_DURATION_OVERHEAD_MS
        private var baseUrlTemplate: String = DEFAULT_BASE_URL_TEMPLATE
        private var clock: Clock = SystemClock()

        /** Retries after the initial attempt; only 429 and 5xx responses are retried. */
        public fun maxRetries(value: Int): Builder = apply {
            require(value >= 0) { "maxRetries must be >= 0, was $value" }
            maxRetries = value
        }

        /**
         * Fraction (0, 1] of each rate-limit bucket spendable as an immediate burst.
         * High values favor latency, low values favor sustained throughput.
         */
        public fun burstFactor(value: Double): Builder = apply {
            require(value > 0.0 && value <= 1.0) { "burstFactor must be in (0, 1], was $value" }
            burstFactor = value
        }

        /** Padding added to each bucket window to absorb clock skew and in-flight latency. */
        public fun durationOverheadMs(value: Long): Builder = apply {
            require(value >= 0) { "durationOverheadMs must be >= 0, was $value" }
            durationOverheadMs = value
        }

        /** URL template; `{route}` is replaced with the route subdomain. */
        public fun baseUrlTemplate(value: String): Builder = apply { baseUrlTemplate = value }

        public fun clock(value: Clock): Builder = apply { clock = value }

        public fun build(): Riot4KConfig {
            require(apiKey.isNotBlank()) { "apiKey must not be blank" }
            return Riot4KConfig(apiKey, maxRetries, burstFactor, durationOverheadMs, baseUrlTemplate, clock)
        }
    }

    public companion object {
        public const val DEFAULT_BASE_URL_TEMPLATE: String = "https://{route}.api.riotgames.com"
        public const val DEFAULT_MAX_RETRIES: Int = 3

        /** Burst preset: spend rate limits as fast as possible, minimizing latency. */
        public const val BURST_BURST_FACTOR: Double = 0.99
        public const val BURST_DURATION_OVERHEAD_MS: Long = 989

        /** Throughput preset: spread requests evenly, minimizing 429s under sustained load. */
        public const val THROUGHPUT_BURST_FACTOR: Double = 0.47
        public const val THROUGHPUT_DURATION_OVERHEAD_MS: Long = 10

        /** Default configuration (the burst preset). */
        public fun of(apiKey: String): Riot4KConfig = Builder(apiKey).build()

        public fun burst(apiKey: String): Riot4KConfig = Builder(apiKey)
            .burstFactor(BURST_BURST_FACTOR)
            .durationOverheadMs(BURST_DURATION_OVERHEAD_MS)
            .build()

        public fun throughput(apiKey: String): Riot4KConfig = Builder(apiKey)
            .burstFactor(THROUGHPUT_BURST_FACTOR)
            .durationOverheadMs(THROUGHPUT_DURATION_OVERHEAD_MS)
            .build()
    }
}
