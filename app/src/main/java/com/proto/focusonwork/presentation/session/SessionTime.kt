package com.proto.focusonwork.presentation.session

import kotlin.math.max

fun remainingSeconds(endsAtMillis: Long, nowMillis: Long): Long =
    max(0L, (endsAtMillis - nowMillis + 999L) / 1_000L)

fun formatRemaining(seconds: Long): String {
    val hours = seconds / 3_600
    val minutes = (seconds % 3_600) / 60
    val remainingSeconds = seconds % 60
    return "%02d : %02d : %02d".format(hours, minutes, remainingSeconds)
}
