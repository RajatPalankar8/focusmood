package com.proto.focusonwork.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proto.focusonwork.data.local.SessionLogEntity
import com.proto.focusonwork.ui.theme.FocusIndigo
import com.proto.focusonwork.ui.theme.FocusPink
import com.proto.focusonwork.ui.theme.FocusViolet
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@Composable
fun FocusStatsScreen(
    sessions: List<SessionLogEntity>,
    modifier: Modifier = Modifier,
    protectedAppNames: List<String> = emptyList(),
    onBack: () -> Unit
) {
    val focusedMinutes = sessions.sumOf { it.durationMinutes }
    val averageMinutes = if (sessions.isEmpty()) 0 else focusedMinutes / sessions.size
    val dailyMinutes = lastSevenDays(sessions)
    val totalBlockedApps = sessions.sumOf { it.blockedAppCount }
    val protectedApps = protectedAppNames.takeIf { it.isNotEmpty() }?.joinToString(", ")
        ?: if (totalBlockedApps > 0) "$totalBlockedApps apps" else "your selected apps"
    val scrollState = rememberScrollState()

    Column(
        modifier.fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .consumeWindowInsets(WindowInsets.safeDrawing)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    "YOUR PROGRESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text("Focus History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Surface(
                shape = CircleShape,
                color = FocusIndigo.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📊", fontSize = 20.sp)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Sessions", sessions.size.toString(), "⚡", Modifier.weight(1f))
            StatCard("Focused", "${focusedMinutes}m", "🎯", Modifier.weight(1f))
            StatCard("Daily Avg", "${averageMinutes}m", "🔥", Modifier.weight(1f))
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Weekly Focus Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Minutes protected per day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FocusIndigo.copy(alpha = 0.15f)
                    ) {
                        Text("7 Days", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = FocusIndigo, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                FocusTrendChart(dailyMinutes, Modifier.fillMaxWidth().height(220.dp))
            }
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                FocusIndigo.copy(alpha = 0.08f),
                                FocusViolet.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FocusIndigo,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⏳", fontSize = 24.sp)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Time Reclaimed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Focus On Work kept $protectedApps away for $focusedMinutes minutes across completed sessions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Completed Sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (sessions.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌱", fontSize = 36.sp)
                        Text("No sessions recorded yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Start a focus session to build your productivity timeline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sessions.forEach { session ->
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.endedAtMillis)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${session.durationMinutes} min protected • ${session.blockedAppCount} apps paused",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = FocusIndigo.copy(alpha = 0.1f)
                                ) {
                                    Text("✓", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = FocusIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTrendChart(values: List<Int>, modifier: Modifier = Modifier) {
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(15)
    val labels = listOf("6d", "5d", "4d", "3d", "2d", "Yest", "Today")
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { _, value ->
                val barFraction = (value.toFloat() / maxValue).coerceIn(0.08f, 1f)
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        if (value > 0) "${value}m" else "-",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value > 0) FocusIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.fillMaxWidth(0.65f)
                            .height(140.dp * barFraction)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                            .background(
                                if (value > 0) {
                                    Brush.verticalGradient(listOf(FocusIndigo, FocusViolet))
                                } else {
                                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                }
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEach { label ->
                Text(
                    label,
                    Modifier.weight(1f),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun lastSevenDays(sessions: List<SessionLogEntity>): List<Int> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.timeInMillis
    return (6 downTo 0).map { daysAgo ->
        val start = today - daysAgo * 86_400_000L
        val end = start + 86_400_000L
        sessions.filter { it.endedAtMillis in start until end }.sumOf { it.durationMinutes }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(icon, fontSize = 16.sp)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

