package com.proto.focusonwork.data.local

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SessionLogRepository(context: Context) {
    private val dao = FocusDatabase.create(context.applicationContext).focusDao()

    val completedSessions: Flow<List<SessionLogEntity>> = dao.observeCompletedSessions()

    suspend fun recordCompletedSession(
        startedAtMillis: Long,
        endedAtMillis: Long,
        durationMinutes: Int,
        blockedAppCount: Int
    ) {
        dao.insertSessionLog(
            SessionLogEntity(
                startedAtMillis = startedAtMillis,
                endedAtMillis = endedAtMillis,
                durationMinutes = durationMinutes,
                wasCompleted = true,
                blockedAppCount = blockedAppCount
            )
        )
    }
}
