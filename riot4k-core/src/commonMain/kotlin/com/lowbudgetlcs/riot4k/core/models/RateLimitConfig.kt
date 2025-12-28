package com.lowbudgetlcs.riot4k.core.models

import kotlin.math.floor


data class RateLimitConfig(
    val limit: Int,
    val windowMs: Long,
    val burstFactor: Double = 0.8
) {
    val effectiveLimit: Int = floor(limit * burstFactor).toInt()
}
