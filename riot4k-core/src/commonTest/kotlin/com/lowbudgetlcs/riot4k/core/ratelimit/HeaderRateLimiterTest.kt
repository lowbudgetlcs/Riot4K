package com.lowbudgetlcs.riot4k.core.ratelimit

import com.lowbudgetlcs.riot4k.core.SchedulerClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HeaderRateLimiterTest {
    private val okHeaders: (String) -> String? = { name ->
        when (name) {
            RateLimitHeaders.APP_RATE_LIMIT -> "100:1"
            RateLimitHeaders.METHOD_RATE_LIMIT -> "100:1"
            else -> null
        }
    }

    @Test
    fun seedBucketThrottlesUntilLimitsAreLearned() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        assertEquals(0, currentTime)
        // No headers seen yet: the 1-per-second seed bucket gates the second call.
        limiter.acquire("americas", "m")
        assertEquals(1000, currentTime)
    }

    @Test
    fun learnedLimitsReplaceTheSeedBucket() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200, okHeaders)
        val before = currentTime
        repeat(50) { limiter.acquire("americas", "m") }
        assertEquals(before, currentTime)
    }

    @Test
    fun appTierGatesEvenWhenMethodTierHasRoom() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200) { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "2:1"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "100:1"
                else -> null
            }
        }
        limiter.acquire("americas", "m")
        limiter.acquire("americas", "m")
        val before = currentTime
        limiter.acquire("americas", "m")
        assertTrue(currentTime > before, "third acquire should wait on the app tier")
    }

    @Test
    fun methodTierGatesEvenWhenAppTierHasRoom() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200) { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "100:1"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "1:1"
                else -> null
            }
        }
        limiter.acquire("americas", "m")
        val before = currentTime
        limiter.acquire("americas", "m")
        assertTrue(currentTime > before, "second acquire should wait on the method tier")
    }

    @Test
    fun appTierIsSharedAcrossMethodsOfARoute() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        val headers: (String) -> String? = { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "2:1"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "100:1"
                else -> null
            }
        }
        limiter.acquire("americas", "a")
        limiter.onResponse("americas", "a", 200, headers)
        limiter.acquire("americas", "a")
        limiter.acquire("americas", "a")
        // Different method, same route: still blocked by the shared app tier
        // (its own method tier learns limits from the same headers).
        limiter.onResponse("americas", "b", 200, headers)
        val before = currentTime
        limiter.acquire("americas", "b")
        assertTrue(currentTime > before, "app tier should gate all methods on the route")
    }

    @Test
    fun routesAreIndependent() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200) { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "1:1"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "1:1"
                else -> null
            }
        }
        limiter.acquire("americas", "m")
        // europe is untouched by americas' limits; only its own seed bucket applies.
        val before = currentTime
        limiter.acquire("europe", "m")
        assertEquals(before, currentTime)
    }

    @Test
    fun applicationRateLimitViolationPenalizesOnlyTheAppTier() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200, okHeaders)
        limiter.onResponse("americas", "m", 429) { name ->
            when (name) {
                RateLimitHeaders.RATE_LIMIT_TYPE -> "application"
                RateLimitHeaders.RETRY_AFTER -> "3"
                else -> null
            }
        }
        val before = currentTime
        limiter.acquire("americas", "m")
        assertEquals(before + 3000, currentTime)
    }

    @Test
    fun methodRateLimitViolationLeavesOtherMethodsUnaffected() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "a")
        limiter.onResponse("americas", "a", 200, okHeaders)
        limiter.onResponse("americas", "b", 200, okHeaders)
        limiter.onResponse("americas", "a", 429) { name ->
            when (name) {
                RateLimitHeaders.RATE_LIMIT_TYPE -> "method"
                RateLimitHeaders.RETRY_AFTER -> "5"
                else -> null
            }
        }
        // Method b on the same route is not blocked.
        val before = currentTime
        limiter.acquire("americas", "b")
        assertEquals(before, currentTime)
        // Method a is.
        limiter.acquire("americas", "a")
        assertEquals(before + 5000, currentTime)
    }

    @Test
    fun missingRetryAfterFallsBackToDefault() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200, okHeaders)
        limiter.onResponse("americas", "m", 429) { name ->
            when (name) {
                RateLimitHeaders.RATE_LIMIT_TYPE -> "service"
                else -> null
            }
        }
        val before = currentTime
        limiter.acquire("americas", "m")
        assertEquals(before + 1000, currentTime)
    }

    @Test
    fun allEntriesOfAMultiEntryLimitAreEnforced() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200) { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "2:1,3:10"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "100:1"
                else -> null
            }
        }
        limiter.acquire("americas", "m")
        limiter.acquire("americas", "m")
        // Third within the same second: 2:1 gates until t+1s.
        val start = currentTime
        limiter.acquire("americas", "m")
        assertEquals(start + 1000, currentTime)
        // Fourth: the 3:10 bucket is now full; it frees when its oldest token expires.
        limiter.acquire("americas", "m")
        assertEquals(start + 10_000, currentTime)
    }

    @Test
    fun changedLimitsRebuildBuckets() = runTest {
        val limiter = HeaderRateLimiter(SchedulerClock(testScheduler))
        limiter.acquire("americas", "m")
        limiter.onResponse("americas", "m", 200) { name ->
            when (name) {
                RateLimitHeaders.APP_RATE_LIMIT -> "1:1"
                RateLimitHeaders.METHOD_RATE_LIMIT -> "100:1"
                else -> null
            }
        }
        limiter.acquire("americas", "m")
        // Limits change (e.g. key upgraded): much larger app limit applies.
        limiter.onResponse("americas", "m", 200, okHeaders)
        val before = currentTime
        repeat(20) { limiter.acquire("americas", "m") }
        assertEquals(before, currentTime)
    }
}
