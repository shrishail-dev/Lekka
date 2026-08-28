package com.nanokernel.expensetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nanokernel.expensetracker.util.CurrencyFormatter

data class DonutSlice(val label: String, val emoji: String, val value: Double, val color: Color, val percent: Int)

/**
 * A ring chart drawn with Canvas — no external charting library needed for a single donut/bar
 * view. [centerContent] renders inside the hole (e.g. the total amount), matching the classic
 * "amount in the middle of the ring" donut layout.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 90f,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val total = slices.sumOf { it.value }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (total <= 0.0) return@Canvas
            var startAngle = -90f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            slices.forEach { slice ->
                val sweep = (slice.value / total * 360f).toFloat()
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        centerContent()
    }
}

/**
 * One block per category: an icon, the name and amount on top, and a full-width bar below
 * sized to its percentage of the month's total, with the percentage printed after it.
 */
@Composable
fun DonutLegend(slices: List<DonutSlice>, currencySymbol: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(slice.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slice.emoji, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(slice.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                        Text(
                            CurrencyFormatter.format(slice.value, currencySymbol),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val fraction = (slice.percent / 100f).coerceIn(0f, 1f).coerceAtLeast(0.02f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(slice.color)
                            )
                        }
                        Text(
                            "${slice.percent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .width(44.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

data class BarEntry(val label: String, val value: Double, val isCurrent: Boolean = false)

/**
 * Compares total spend across months side by side, so a rising or falling trend is visible
 * at a glance. The current month's bar is highlighted; each bar is labeled with its amount
 * so the chart is readable without cross-referencing anything else.
 * Requires a bounded-height parent (e.g. Modifier.height(160.dp)) since bars scale to fillMaxHeight(fraction).
 */
@Composable
fun SimpleBarChart(
    entries: List<BarEntry>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = (entries.maxOfOrNull { it.value } ?: 0.0).coerceAtLeast(1.0)
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        entries.forEach { entry ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.formatCompact(entry.value, currencySymbol),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (entry.isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.isCurrent) highlightColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                val fraction = (entry.value / maxValue).toFloat().coerceIn(0f, 1f).coerceAtLeast(0.02f)
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .fillMaxHeight(fraction)
                        .background(
                            if (entry.isCurrent) highlightColor else barColor.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (entry.isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
