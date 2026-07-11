package com.lowbudgetlcs.riot4k.core

import com.lowbudgetlcs.riot4k.core.models.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * [Clock] that follows a `runTest` scheduler's virtual time, so `delay()` calls
 * inside the code under test advance the clock consistently.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun nowMillis(): Long = scheduler.currentTime
}
