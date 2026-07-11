package com.lowbudgetlcs.riot4k.core.errors

/**
 * Thrown only by the Kotlin convenience facades ([com.lowbudgetlcs.riot4k.core.result.getOrNull]
 * and [com.lowbudgetlcs.riot4k.core.result.getOrThrow]); the canonical API surface returns
 * [com.lowbudgetlcs.riot4k.core.result.RiotResult] values and never throws across
 * language boundaries.
 */
public open class RiotApiException(
    message: String,
    public val statusCode: Int?,
    public val retries: Int,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public class RiotRateLimitException(
    message: String,
    retries: Int,
    public val rateLimitType: String?,
    cause: Throwable? = null,
) : RiotApiException(message, RATE_LIMIT_STATUS, retries, cause) {
    private companion object {
        const val RATE_LIMIT_STATUS = 429
    }
}
