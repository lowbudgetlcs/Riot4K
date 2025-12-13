package com.lowbudgetlcs.riot4K

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmSequenceTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(5, generateSequence().take(3).last())
    }
}
