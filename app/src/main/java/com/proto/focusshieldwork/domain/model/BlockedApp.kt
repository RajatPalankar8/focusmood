package com.proto.focusshieldwork.domain.model

data class BlockedApp(
    val packageName: String,
    val displayName: String,
    val category: AppCategory = AppCategory.OTHER,
    val isProtected: Boolean = false
)

enum class AppCategory {
    SOCIAL,
    VIDEO,
    GAMES,
    SHOPPING,
    OTHER
}
