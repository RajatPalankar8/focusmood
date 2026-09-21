package com.proto.focusonwork.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val category: String = "OTHER",
    val isProtected: Boolean = false
)
