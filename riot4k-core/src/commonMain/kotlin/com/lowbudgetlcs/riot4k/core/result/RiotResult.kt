package com.lowbudgetlcs.riot4k.core.result

import com.lowbudgetlcs.riot4k.core.errors.RiotApiException
import com.lowbudgetlcs.riot4k.core.errors.RiotRateLimitException

/**
 * Canonical outcome of every Riot API call. Errors travel as values (never as
 * exceptions) so they stay typed across the Kotlin/Swift/JS boundaries; Kotlin
 * callers who prefer exception style use [getOrNull]/[getOrThrow].
 */
public sealed class RiotResult<out T> {
    /** The request succeeded and returned a body. */
    public data class Success<T>(val data: T) : RiotResult<T>()

    /**
     * The request succeeded but the resource does not exist (HTTP 404/204).
     * A first-class empty result — not a failure.
     */
    public data object NotFound : RiotResult<Nothing>()

    /** The request failed after [retries] retries. */
    public data class Failure(
        val statusCode: Int?,
        val retries: Int,
        val rateLimitType: String?,
        val message: String,
        val cause: Throwable? = null,
    ) : RiotResult<Nothing>()
}

/** [T] on success, `null` on [RiotResult.NotFound]; throws [RiotApiException] on failure. */
public fun <T> RiotResult<T>.getOrNull(): T? = when (this) {
    is RiotResult.Success -> data
    is RiotResult.NotFound -> null
    is RiotResult.Failure -> throw toException()
}

/** [T] on success; throws [RiotApiException] on [RiotResult.NotFound] and failure. */
public fun <T> RiotResult<T>.getOrThrow(): T = when (this) {
    is RiotResult.Success -> data
    is RiotResult.NotFound -> throw RiotApiException("Resource not found", STATUS_NOT_FOUND, retries = 0)
    is RiotResult.Failure -> throw toException()
}

private fun RiotResult.Failure.toException(): RiotApiException = when (statusCode) {
    STATUS_TOO_MANY_REQUESTS -> RiotRateLimitException(message, retries, rateLimitType, cause)
    else -> RiotApiException(message, statusCode, retries, cause)
}

private const val STATUS_NOT_FOUND = 404
private const val STATUS_TOO_MANY_REQUESTS = 429
