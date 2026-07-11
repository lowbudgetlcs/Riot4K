package com.lowbudgetlcs.riot4k.core.ratelimit

import com.lowbudgetlcs.riot4k.core.models.RateLimitConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorTokenBucketTest {
    @Test
    fun freshBucketHasNoDelayAndFullCapacity() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 5, windowMs = 1000))
        assertEquals(0, bucket.getDelay(nowMs = 0))
        assertEquals(1.0, bucket.getCapacity(nowMs = 0))
    }

    @Test
    fun capacityDecreasesAsTokensAreTaken() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 4, windowMs = 1000))
        bucket.getTokens(nowMs = 0)
        assertEquals(0.75, bucket.getCapacity(nowMs = 0))
        bucket.getTokens(nowMs = 0, n = 2)
        assertEquals(0.25, bucket.getCapacity(nowMs = 0))
    }

    @Test
    fun exhaustedBucketDelaysUntilOldestTokenExpires() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 2, windowMs = 1000))
        bucket.getTokens(nowMs = 0)
        bucket.getTokens(nowMs = 400)
        assertEquals(0.0, bucket.getCapacity(nowMs = 400))
        // Oldest token (t=0) frees a slot at t=1000.
        assertEquals(600, bucket.getDelay(nowMs = 400))
    }

    @Test
    fun tokensExpireAsTheWindowSlides() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 2, windowMs = 1000))
        bucket.getTokens(nowMs = 0, n = 2)
        assertTrue(bucket.getDelay(nowMs = 999) > 0)
        assertEquals(0, bucket.getDelay(nowMs = 1000))
        assertEquals(1.0, bucket.getCapacity(nowMs = 1000))
    }

    @Test
    fun burstFactorLimitsSpendWithinTheBurstWindow() {
        // 10 per 10s with burstFactor 0.5: at most 5 inside any 5s burst window.
        val bucket = VectorTokenBucket(
            RateLimitConfig(limit = 10, windowMs = 10_000, burstFactor = 0.5),
        )
        bucket.getTokens(nowMs = 0, n = 5)
        // Burst limit reached: the 5th-newest token (t=0) frees burst room at t=5000.
        assertEquals(5000, bucket.getDelay(nowMs = 0))
        assertEquals(0, bucket.getDelay(nowMs = 5000))

        // Spend the rest of the full limit; now the full window gates.
        bucket.getTokens(nowMs = 5000, n = 5)
        assertEquals(5000, bucket.getDelay(nowMs = 5000))
        assertEquals(0, bucket.getDelay(nowMs = 10_000))
    }

    @Test
    fun burstLimitIsAtLeastOne() {
        val bucket = VectorTokenBucket(
            RateLimitConfig(limit = 1, windowMs = 1000, burstFactor = 0.1),
        )
        // floor(1 * 0.1) would be 0; a bucket must always allow one request through.
        assertEquals(0, bucket.getDelay(nowMs = 0))
        bucket.getTokens(nowMs = 0)
        assertTrue(bucket.getDelay(nowMs = 0) > 0)
    }

    @Test
    fun durationOverheadPadsTheWindow() {
        val bucket = VectorTokenBucket(
            RateLimitConfig(limit = 1, windowMs = 1000),
            durationOverheadMs = 500,
        )
        bucket.getTokens(nowMs = 0)
        assertEquals(500, bucket.getDelay(nowMs = 1000))
        assertEquals(0, bucket.getDelay(nowMs = 1500))
    }

    @Test
    fun delayAccountsForNewerTokensAfterOldestExpires() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 2, windowMs = 1000))
        bucket.getTokens(nowMs = 0)
        bucket.getTokens(nowMs = 800)
        // At t=1100 the t=0 token expired; one slot free.
        assertEquals(0, bucket.getDelay(nowMs = 1100))
        bucket.getTokens(nowMs = 1100)
        // Full again; the t=800 token frees a slot at t=1800.
        assertEquals(700, bucket.getDelay(nowMs = 1100))
    }

    @Test
    fun capacityNeverGoesNegative() {
        val bucket = VectorTokenBucket(RateLimitConfig(limit = 2, windowMs = 1000))
        bucket.getTokens(nowMs = 0, n = 5)
        assertEquals(0.0, bucket.getCapacity(nowMs = 0))
    }

    @Test
    fun fullWindowLimitDominatesBurstDelay() {
        // Overfilled bucket: full-window delay must be returned, not just burst delay.
        val bucket = VectorTokenBucket(
            RateLimitConfig(limit = 3, windowMs = 3000, burstFactor = 1.0),
        )
        bucket.getTokens(nowMs = 0, n = 3)
        assertEquals(3000, bucket.getDelay(nowMs = 0))
        assertEquals(1000, bucket.getDelay(nowMs = 2000))
    }
}
