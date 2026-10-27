package com.proto.focusshieldwork.domain.model

data class FocusPreset(
    val id: Long = 0L,
    val name: String,
    val durationMinutes: Int,
    val blockedPackages: Set<String>
)
