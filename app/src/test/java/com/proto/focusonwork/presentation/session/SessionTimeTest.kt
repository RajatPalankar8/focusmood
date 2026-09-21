package com.proto.focusonwork.presentation.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTimeTest {
    @Test
    fun remainingSeconds_roundsUpUntilTheEnd() {
        assertEquals(61L, remainingSeconds(61_000L, 0L))
        assertEquals(0L, remainingSeconds(1_000L, 1_000L))
        assertEquals(0L, remainingSeconds(0L, 1_000L))
    }

    @Test
    fun formatRemaining_includesHoursMinutesAndSeconds() {
        assertEquals("01 : 02 : 03", formatRemaining(3_723L))
    }
}
