package com.lowbudgetlcs.riot4k.core.ratelimit

/** One `count:seconds` entry of an `X-App-Rate-Limit` / `X-Method-Rate-Limit` header. */
public data class RateLimitEntry(
    val count: Int,
    val windowSeconds: Int,
)

public object RateLimitHeaders {
    public const val APP_RATE_LIMIT: String = "X-App-Rate-Limit"
    public const val METHOD_RATE_LIMIT: String = "X-Method-Rate-Limit"
    public const val RATE_LIMIT_TYPE: String = "X-Rate-Limit-Type"
    public const val RETRY_AFTER: String = "Retry-After"

    /**
     * Parses `"20:1,100:120"` into entries; malformed pairs are skipped so a
     * partially-garbled header can't take the client down.
     */
    public fun parse(value: String?): List<RateLimitEntry> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(',').mapNotNull { pair ->
            val parts = pair.trim().split(':')
            if (parts.size != 2) return@mapNotNull null
            val count = parts[0].trim().toIntOrNull() ?: return@mapNotNull null
            val seconds = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
            if (count <= 0 || seconds <= 0) return@mapNotNull null
            RateLimitEntry(count, seconds)
        }
    }
}
