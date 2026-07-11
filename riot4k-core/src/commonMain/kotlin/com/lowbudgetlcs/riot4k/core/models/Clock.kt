package com.lowbudgetlcs.riot4k.core.models

/**
 * Time source abstraction so rate-limiting and retry logic are fully testable.
 */
public interface Clock {
    public fun nowMillis(): Long
}
