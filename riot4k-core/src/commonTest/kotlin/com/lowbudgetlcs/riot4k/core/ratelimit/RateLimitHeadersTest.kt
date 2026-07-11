package com.lowbudgetlcs.riot4k.core.ratelimit

import kotlin.test.Test
import kotlin.test.assertEquals

class RateLimitHeadersTest {
    @Test
    fun parsesSingleEntry() {
        assertEquals(listOf(RateLimitEntry(20, 1)), RateLimitHeaders.parse("20:1"))
    }

    @Test
    fun parsesMultipleEntries() {
        assertEquals(
            listOf(RateLimitEntry(20, 1), RateLimitEntry(100, 120)),
            RateLimitHeaders.parse("20:1,100:120"),
        )
    }

    @Test
    fun toleratesWhitespace() {
        assertEquals(
            listOf(RateLimitEntry(20, 1), RateLimitEntry(100, 120)),
            RateLimitHeaders.parse(" 20 : 1 , 100 : 120 "),
        )
    }

    @Test
    fun returnsEmptyForNull() {
        assertEquals(emptyList(), RateLimitHeaders.parse(null))
    }

    @Test
    fun returnsEmptyForBlank() {
        assertEquals(emptyList(), RateLimitHeaders.parse("  "))
    }

    @Test
    fun skipsMalformedPairs() {
        assertEquals(
            listOf(RateLimitEntry(100, 120)),
            RateLimitHeaders.parse("garbage,100:120"),
        )
        assertEquals(
            listOf(RateLimitEntry(20, 1)),
            RateLimitHeaders.parse("20:1,100:120:7"),
        )
        assertEquals(emptyList(), RateLimitHeaders.parse("a:b,:,"))
    }

    @Test
    fun skipsNonPositiveValues() {
        assertEquals(emptyList(), RateLimitHeaders.parse("0:1,-5:10,20:0"))
    }
}
