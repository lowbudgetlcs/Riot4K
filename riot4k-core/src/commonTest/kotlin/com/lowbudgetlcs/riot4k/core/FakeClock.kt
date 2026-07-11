package com.lowbudgetlcs.riot4k.core

import com.lowbudgetlcs.riot4k.core.models.Clock

/** Manually-advanced [Clock] for deterministic time-based tests. */
class FakeClock(var nowMs: Long = 0) : Clock {
    override fun nowMillis(): Long = nowMs

    fun advance(ms: Long) {
        nowMs += ms
    }
}
