package com.lowbudgetlcs.riot4k

import kotlin.test.Test
import kotlin.test.assertEquals

class IosSequenceTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(7, generateSequence().take(3).last())
    }
}
