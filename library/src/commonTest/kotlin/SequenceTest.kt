package com.lowbudgetlcs.riot4k

import kotlin.test.Test
import kotlin.test.assertEquals

class SequenceTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(firstElement + secondElement, generateSequence().take(3).last())
    }
}
