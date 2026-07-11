package com.lowbudgetlcs.riot4k.core.ratelimit

/**
 * Gate in front of every outgoing request. Deliberately Ktor-free so
 * implementations are testable without HTTP types.
 */
public interface RateLimiter {
    /** Suspends until a request for ([routeKey], [methodKey]) may proceed. */
    public suspend fun acquire(routeKey: String, methodKey: String)

    /**
     * Feeds a response back so limits can be learned from Riot's headers.
     * [headers] is a case-insensitive header lookup.
     */
    public suspend fun onResponse(
        routeKey: String,
        methodKey: String,
        statusCode: Int,
        headers: (String) -> String?,
    )
}
