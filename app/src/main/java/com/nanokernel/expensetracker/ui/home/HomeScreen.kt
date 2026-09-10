package com.nanokernel.expensetracker.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.ui.components.CategoryIcon
import com.nanokernel.expensetracker.ui.components.DeleteExpenseDialog
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.ui.components.ScreenHeader
import com.nanokernel.expensetracker.ui.components.StatCard
import com.nanokernel.expensetracker.ui.theme.colorFor
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils
import java.time.YearMonth

@Composable
fun HomeScreen(
    onExpenseClick: (Long) -> Unit,
    onBorrowedClick: () -> Unit,
    onEventsClick: () -> Unit,
    onLentClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    app.repository,
                    app.borrowRepository,
                    app.eventRepository,
                    app.eventExpenseRepository,
                    app.lentRepository,
                    app.settingsRepository
                )
            }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var showBudgetDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Only place in the app that proactively asks for POST_NOTIFICATIONS — needed so the daily
    // "log your expenses" reminder (armed on app start, see ExpenseTrackerApp) can actually show.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op either way — declining just means the reminder silently won't show */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            onDismiss = { expenseToDelete = null },
            onConfirm = { viewModel.deleteExpense(expense); expenseToDelete = null }
        )
    }

    if (showBudgetDialog) {
        EditAmountDialog(
            title = "Monthly Budget",
            helperText = "Used to work out your balance and how much of it you've spent.",
            currentValue = state.budget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { viewModel.setBudget(it); showBudgetDialog = false }
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ScreenHeader("Lekka") }

        item {
            // Balance gets real visual priority as the one number that matters most at a
            // glance — Spent/Budget are supporting detail underneath, not equal-weight columns.
            // The whole card recolors to the error tone when over budget, so "you're over" is
            // visible without reading any numbers.
            val overBudget = state.balance < 0
            val heroColor = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val onHeroColor = if (overBudget) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = heroColor),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        DateUtils.formatMonthLabel(YearMonth.now()),
                        style = MaterialTheme.typography.titleSmall,
                        color = onHeroColor.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (overBudget) "OVER BUDGET" else "BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = onHeroColor.copy(alpha = 0.75f)
                    )
                    Text(
                        CurrencyFormatter.format(kotlin.math.abs(state.balance), state.currencySymbol),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = onHeroColor,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = onHeroColor.copy(alpha = 0.2f))
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(
                                "SPENT THIS MONTH",
                                style = MaterialTheme.typography.labelSmall,
                                color = onHeroColor.copy(alpha = 0.75f)
                            )
                            Text(
                                CurrencyFormatter.format(state.monthTotal, state.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                color = onHeroColor
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showBudgetDialog = true }
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "BUDGET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onHeroColor.copy(alpha = 0.75f)
                                )
                                Text(
                                    CurrencyFormatter.format(state.budget, state.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = onHeroColor
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit budget",
                                tint = onHeroColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Today",
                    value = CurrencyFormatter.format(state.todayTotal, state.currencySymbol),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "This Week",
                    value = CurrencyFormatter.format(state.weekTotal, state.currencySymbol),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Borrowed/Lent paired first — both are the same "money owed" concept, just in
                // opposite directions — with Events as its own full-width row below since it's
                // an unrelated concept (event budgets, not debts).
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TrackerCard(
                        emoji = "💸",
                        iconColor = MaterialTheme.colorScheme.error,
                        label = "BORROWED",
                        value = CurrencyFormatter.format(state.borrowed, state.currencySymbol),
                        valueColor = MaterialTheme.colorScheme.error,
                        onClick = onBorrowedClick,
                        modifier = Modifier.weight(1f)
                    )
                    TrackerCard(
                        emoji = "🤝",
                        iconColor = MaterialTheme.colorScheme.primary,
                        label = "LENT",
                        value = CurrencyFormatter.format(state.lent, state.currencySymbol),
                        valueColor = MaterialTheme.colorScheme.primary,
                        onClick = onLentClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                TrackerCard(
                    emoji = "🎉",
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    label = if (state.activeEventCount > 0) "EVENTS (${state.activeEventCount})" else "EVENTS",
                    value = CurrencyFormatter.format(state.activeEventsTotal, state.currencySymbol),
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onEventsClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Column {
                Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }

        if (state.recentExpenses.isEmpty()) {
            item {
                Text("No expenses yet. Tap + to add your first one.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            item {
                // A plain Column, not items(), so rows are only as far apart as their own
                // tight padding — the outer 20dp spacing is for sections, not individual rows.
                Column {
                    state.recentExpenses.forEach { expense ->
                        val category = state.categories.findById(expense.category)
                        val hasNote = expense.note?.isNotBlank() == true
                        val dateText = DateUtils.formatDay(expense.timestampMillis)
                        ExpenseListRow(
                            emoji = category.emoji,
                            color = state.categories.colorFor(category.id),
                            title = expense.note?.takeIf { it.isNotBlank() } ?: category.displayName,
                            // When the note is shown as the title, the category name would
                            // otherwise disappear entirely — shown here instead so two custom
                            // categories that happen to share an emoji stay distinguishable.
                            subtitle = if (hasNote) "${category.displayName} · $dateText" else dateText,
                            amountText = CurrencyFormatter.format(expense.amount, state.currencySymbol),
                            onClick = { onExpenseClick(expense.id) },
                            onLongClick = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

/** Shared by Borrowed/Lent/Events — a leading emoji icon, a small label + value, and a chevron.
 *  Flat filled card (no border) to match the friendlier, less bordered-ledger look. */
@Composable
private fun TrackerCard(
    emoji: String,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(emoji, iconColor, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            // weight(1f) + single-line text guarantees the same row height (and chevron
            // position) across cards regardless of which one's text is longer.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EditAmountDialog(
    title: String,
    helperText: String,
    currentValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(currentValue.toInt().toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(helperText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.toDoubleOrNull() ?: currentValue) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
