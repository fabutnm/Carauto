package com.example.carexpenses.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carexpenses.MonthTotal
import com.example.carexpenses.Period
import com.example.carexpenses.computeSummary
import com.example.carexpenses.data.Expense
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(expenses: List<Expense>, modifier: Modifier = Modifier) {
    var period by rememberSaveable { mutableStateOf(Period.ALL) }
    val summary = remember(expenses, period) { computeSummary(expenses, period) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Period.values().forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p.label) }
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Всего", formatMoney(summary.total), Modifier.weight(1f))
                StatTile("На топливо", formatMoney(summary.fuelCost), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    "Расход, л/100 км",
                    summary.avgConsumption?.let(::fmt1) ?: "—",
                    Modifier.weight(1f)
                )
                StatTile(
                    "Стоимость 1 км",
                    summary.costPerKm?.let { "${fmt1(it)} ₽" } ?: "—",
                    Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    "Пробег за период",
                    summary.distanceKm?.let(::formatKm) ?: "—",
                    Modifier.weight(1f)
                )
                StatTile(
                    "Средняя цена литра",
                    summary.avgPricePerLiter?.let { "${fmt1(it)} ₽" } ?: "—",
                    Modifier.weight(1f)
                )
            }
        }
        item {
            SectionCard("Расходы по месяцам") {
                MonthBars(summary.byMonth)
            }
        }
        item {
            SectionCard("Расход топлива, л/100 км") {
                if (summary.consumptionSeries.size < 2) {
                    Text(
                        "Нужно минимум три полные заправки с указанием пробега и литров.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LineChart(summary.consumptionSeries)
                }
            }
        }
        item {
            SectionCard("По категориям") {
                if (summary.byType.isEmpty()) {
                    Text("Нет данных за выбранный период.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val max = summary.byType.maxOf { it.second }.coerceAtLeast(1.0)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        summary.byType.forEach { (type, sum) ->
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${type.emoji} ${type.label}", style = MaterialTheme.typography.bodyMedium)
                                    Text(formatMoney(sum), style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (sum / max).toFloat() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun MonthBars(months: List<MonthTotal>) {
    val max = months.maxOfOrNull { it.total } ?: 0.0
    val barColor = MaterialTheme.colorScheme.primary

    Column {
        Row(
            Modifier.fillMaxWidth().height(160.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            months.forEach { m ->
                val fraction = if (max > 0) (m.total / max).toFloat() else 0f
                Column(
                    Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(fraction * 0.8f)
                                .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Text(
                        m.month.month.getDisplayName(TextStyle.SHORT, Locale("ru")).take(3),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (max > 0) "Максимум за месяц: ${formatMoney(max)}" else "Пока нет данных за последние 12 месяцев.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LineChart(values: List<Double>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val min = values.min()
    val max = values.max()
    val span = if (max - min > 1e-9) max - min else 1.0

    Column {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val pad = 12.dp.toPx()
            val w = size.width - 2 * pad
            val h = size.height - 2 * pad
            val steps = (values.size - 1).coerceAtLeast(1)
            val points = values.mapIndexed { i, v ->
                Offset(
                    x = pad + w * i / steps,
                    y = pad + h * (1f - ((v - min) / span).toFloat())
                )
            }

            drawLine(
                color = axisColor,
                start = Offset(pad, size.height - pad),
                end = Offset(size.width - pad, size.height - pad),
                strokeWidth = 1.dp.toPx()
            )
            val path = Path().apply {
                points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
            }
            drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            points.forEach { drawCircle(lineColor, radius = 4.dp.toPx(), center = it) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("мин ${fmt1(min)}", style = MaterialTheme.typography.bodySmall)
            Text("макс ${fmt1(max)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
