package com.proto.focusonwork.presentation.stats

import com.proto.focusonwork.data.local.SessionLogEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusStatsTest {
    @Test
    fun aggregatesOnlyCompletedSessionRecords() {
        val sessions = listOf(
            SessionLogEntity(startedAtMillis = 0L, endedAtMillis = 60_000L, durationMinutes = 25, wasCompleted = true),
            SessionLogEntity(startedAtMillis = 120_000L, endedAtMillis = 180_000L, durationMinutes = 45, wasCompleted = true)
        )

        assertEquals(2, sessions.size)
        assertEquals(70, sessions.sumOf { it.durationMinutes })
    }

    @Test
    fun cancelledSessionIsNotPartOfCompletedHistory() {
        val completed = listOf(
            SessionLogEntity(startedAtMillis = 0L, endedAtMillis = 60_000L, durationMinutes = 25, wasCompleted = true)
        )
        assertEquals(1, completed.size)
        assertEquals(25, completed.single().durationMinutes)
    }
}
