package com.lowbudgetlcs.riot4K

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidSequenceTest {

    @Test
    fun testThirdElement() {
        assertEquals(3, generateSequence().take(3).last())
    }
}
