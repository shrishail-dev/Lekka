package com.nanokernel.expensetracker.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        DateUtils.formatMonthLabel(YearMonth.now()),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryColumn(
                            label = "Expenses",
                            value = CurrencyFormatter.format(state.monthTotal, state.currencySymbol),
                            valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryColumn(
                            label = "Budget",
                            value = CurrencyFormatter.format(state.budget, state.currencySymbol),
                            valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                            onEdit = { showBudgetDialog = true }
                        )
                        SummaryColumn(
                            label = "Balance",
                            value = CurrencyFormatter.format(state.balance, state.currencySymbol),
                            valueColor = if (state.balance >= 0) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.weight(1f)
                        )
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
                    BorrowedCard(
                        value = CurrencyFormatter.format(state.borrowed, state.currencySymbol),
                        onClick = onBorrowedClick,
                        modifier = Modifier.weight(1f)
                    )
                    LentCard(
                        value = CurrencyFormatter.format(state.lent, state.currencySymbol),
                        onClick = onLentClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                EventsCard(
                    count = state.activeEventCount,
                    value = CurrencyFormatter.format(state.activeEventsTotal, state.currencySymbol),
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

@Composable
private fun SummaryColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        // Fixed height regardless of whether the edit icon is present, so the value text below
        // lines up across columns — Budget's icon would otherwise make its label row taller than
        // Expenses'/Balance's and push its value down out of alignment.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(18.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = valueColor.copy(alpha = 0.8f)
            )
            if (onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(18.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit $label",
                        tint = valueColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun BorrowedCard(value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // weight(1f) + single-line text on both cards guarantees the same row height (and
            // chevron position) on the Home screen regardless of which card's text is longer.
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "BORROWED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View borrowed money",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Mirrors [BorrowedCard] but for the opposite direction — money others owe you. */
@Composable
private fun LentCard(value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "LENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View lent money",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Mirrors [BorrowedCard] — total is active (non-archived) events only, kept separate from
 *  the monthly budget math above; tapping it opens the event list. */
@Composable
private fun EventsCard(count: Int, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (count > 0) "EVENTS ($count)" else "EVENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View events",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
