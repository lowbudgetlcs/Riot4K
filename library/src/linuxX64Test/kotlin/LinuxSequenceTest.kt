package com.lowbudgetlcs.riot4K

import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxSequenceTest {

    @Test
    fun `test 3rd element`() {
        assertEquals(8, generateSequence().take(3).last())
    }
}
