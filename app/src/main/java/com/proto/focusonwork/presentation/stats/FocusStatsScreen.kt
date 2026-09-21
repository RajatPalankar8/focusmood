package com.proto.focusonwork.presentation.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
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
    val protectedApps = protectedAppNames.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "your selected apps"

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
                FocusTrendChart(dailyMinutes, Modifier.fillMaxWidth().height(190.dp).padding(top = 14.dp))
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
    val maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Canvas(modifier) {
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { index, value ->
            Offset(index * step, size.height - (value / maxValue.toFloat()) * size.height)
        }
        for (line in 1..3) {
            val y = size.height * line / 4f
            drawLine(Color.White.copy(alpha = .8f), Offset(0f, y), Offset(size.width, y), 1f)
        }
        if (points.size > 1) {
            val fill = Path().apply { moveTo(points.first().x, size.height); points.forEach { lineTo(it.x, it.y) }; lineTo(points.last().x, size.height); close() }
            drawPath(fill, Brush.verticalGradient(listOf(FocusIndigo.copy(alpha = .32f), Color.Transparent)))
            val line = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }
            drawPath(line, FocusIndigo, style = Stroke(width = 6f, cap = StrokeCap.Round))
            points.forEach { drawCircle(FocusIndigo, 7f, it); drawCircle(Color.White, 3f, it) }
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
