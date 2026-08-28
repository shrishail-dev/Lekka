package com.nanokernel.expensetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanokernel.expensetracker.R
import com.nanokernel.expensetracker.data.model.CategoryInfo

/** The Lekka mark — used on every screen's header for consistent branding. */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    Image(
        painter = painterResource(R.drawable.ic_lekka_logo),
        contentDescription = "Lekka",
        modifier = modifier.size(size)
    )
}

/** Logo + page title, used at the top of every screen. */
@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        AppLogo()
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun CategoryChip(category: CategoryInfo, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(category.label, style = MaterialTheme.typography.labelSmall) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.height(30.dp)
    )
}

/** A category's emoji on a soft tinted circle in its chart color — an at-a-glance category
 *  identity used anywhere an expense is listed, instead of a bare emoji glyph. */
@Composable
fun CategoryIcon(emoji: String, color: Color, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

/**
 * A compact expense row — icon, title/subtitle, amount — used everywhere an expense list
 * appears (Home, Calendar, Report). Deliberately tighter than a Material [androidx.compose.material3.ListItem],
 * whose fixed min-height wastes space when a screen needs to fit many rows.
 *
 * Tap opens the expense's detail page; long-press offers to delete it — no delete icon is
 * shown in the row itself, keeping the list uncluttered.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseListRow(
    emoji: String,
    color: Color,
    title: String,
    subtitle: String,
    amountText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(emoji, color, size = 38.dp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(amountText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** Confirmation shown after a long-press on an [ExpenseListRow], before it's actually deleted. */
@Composable
fun DeleteExpenseDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete expense?") },
        text = { Text("This can't be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun BudgetProgressBar(spent: Double, budget: Double, modifier: Modifier = Modifier) {
    val fraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val overBudget = spent > budget && budget > 0
    LinearProgressIndicator(
        progress = { fraction },
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp)),
        color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

// Deliberately generic-first (🏷️) so someone stuck on "what represents Labour/Repairs/Misc?"
// has an obvious safe default — the category name shown in every list row does the identifying
// work, not the emoji, so picking the wrong one is never actually a problem.
val quickCategoryEmojis = listOf(
    "🏷️", "🔧", "👷", "🧹", "🧾", "🎓", "💊", "🐾", "🏋️", "✈️",
    "🎁", "📱", "🏠", "☕", "🚗", "🎨", "📚", "🎵", "💇", "🧺"
)

/** Shared by Add Expense and Add Event Expense so both can create a category inline instead of
 *  only picking from the existing list. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(quickCategoryEmojis.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickCategoryEmojis.forEach { candidate ->
                        Text(
                            candidate,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .clickable { emoji = candidate }
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Don't see a fit? The name is what identifies the category in your lists — 🏷️ works fine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, emoji) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun InsightCard(emoji: String, text: String, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
