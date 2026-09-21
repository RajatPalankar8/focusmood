package com.proto.focusonwork.domain.model

/** A persisted focus session represented by its wall-clock end time. */
data class FocusSession(
    val durationMinutes: Int,
    val startedAtMillis: Long,
    val endsAtMillis: Long,
    val blockedPackages: Set<String>,
    val status: SessionStatus = SessionStatus.ACTIVE
)

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}
