package com.proto.focusonwork.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationMinutes: Int,
    val wasCompleted: Boolean,
    val emergencyUnlock: Boolean = false,
    val blockedAppCount: Int = 0
)
