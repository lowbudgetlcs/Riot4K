package com.lowbudgetlcs.riot4k.core.models

import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public class SystemClock : Clock {
    override fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}
