package com.proto.focusonwork.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_presets")
data class FocusPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val durationMinutes: Int,
    val blockedPackages: String
)
