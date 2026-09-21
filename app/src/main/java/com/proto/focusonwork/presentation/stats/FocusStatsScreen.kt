package com.proto.focusonwork.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proto.focusonwork.data.local.SessionLogEntity
import com.proto.focusonwork.ui.theme.FocusIndigo
import com.proto.focusonwork.ui.theme.FocusLavender
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        ?: if (totalBlockedApps > 0) "$totalBlockedApps app protections" else "your selected apps"

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
            Text("Focus history", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("▥", style = MaterialTheme.typography.titleLarge, color = FocusIndigo)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Sessions", sessions.size.toString(), Modifier.weight(1f))
            StatCard("Focused", "${focusedMinutes}m", Modifier.weight(1f))
            StatCard("Daily avg", "${averageMinutes}m", Modifier.weight(1f))
        }
        Card(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FocusLavender.copy(alpha = .55f))
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Your focus, day by day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Minutes protected in the last 7 days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FocusTrendChart(dailyMinutes, Modifier.fillMaxWidth().height(250.dp).padding(top = 14.dp))
            }
        }
        Card(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Time reclaimed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Focus Mood kept $protectedApps away for $focusedMinutes minutes across completed sessions.", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("Completed sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        if (sessions.isEmpty()) {
            Text("Complete your first focus session to start building your graph.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(count = sessions.size) { index ->
                    val session = sessions[index]
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.endedAtMillis)), fontWeight = FontWeight.Bold)
                            Text("${session.durationMinutes} minutes protected  •  ${session.blockedAppCount} apps", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val labels = listOf("6d", "5d", "4d", "3d", "2d", "Yesterday", "Today")
    Column(modifier) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            values.forEachIndexed { index, value ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Text("${value}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FocusIndigo)
                    Spacer(Modifier.height(6.dp))
                    Card(
                        Modifier.fillMaxWidth().height((value.toFloat() / maxValue * 150f).coerceAtLeast(10f).dp),
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = FocusIndigo)
                    ) {}
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEach { label -> Text(label, Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
        }
    }
}

private fun lastSevenDays(sessions: List<SessionLogEntity>): List<Int> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.timeInMillis
    return (6 downTo 0).map { daysAgo ->
        val start = today - daysAgo * 86_400_000L
        val end = start + 86_400_000L
        sessions.filter { it.endedAtMillis in start until end }.sumOf { it.durationMinutes }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall) } }
}
