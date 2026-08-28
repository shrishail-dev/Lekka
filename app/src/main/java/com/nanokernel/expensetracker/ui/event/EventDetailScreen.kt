package com.nanokernel.expensetracker.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.data.local.EventExpenseEntity
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.ui.theme.colorFor
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onBack: () -> Unit,
    onEditEvent: (Long) -> Unit,
    onAddExpense: (Long) -> Unit,
    onEditExpense: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: EventDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                EventDetailViewModel(eventId, app.eventRepository, app.eventExpenseRepository, app.settingsRepository)
            }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var expenseToDelete by remember { mutableStateOf<EventExpenseEntity?>(null) }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete expense?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(expense); expenseToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") } }
        )
    }

    val event = state.event

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(event?.emoji ?: "🎉", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            event?.name ?: "Event",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { onEditEvent(eventId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit event")
                    }
                    IconButton(onClick = { onAddExpense(eventId) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add expense")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Total spent", style = MaterialTheme.typography.titleSmall)
                Text(
                    CurrencyFormatter.format(state.total, state.currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            val budget = event?.budget
            if (budget != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val remaining = budget - state.total
                    Text(
                        if (remaining >= 0) {
                            "${CurrencyFormatter.format(remaining, state.currencySymbol)} left"
                        } else {
                            "${CurrencyFormatter.format(-remaining, state.currencySymbol)} over"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.expenses.isEmpty()) {
                Text(
                    "No expenses logged for this event yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(state.expenses, key = { it.id }) { expense ->
                        val category = state.categories.findById(expense.category)
                        val hasNote = expense.note?.isNotBlank() == true
                        val dateText = DateUtils.formatDay(expense.timestampMillis)
                        ExpenseListRow(
                            emoji = category.emoji,
                            color = state.categories.colorFor(category.id),
                            title = expense.note?.takeIf { it.isNotBlank() } ?: category.displayName,
                            subtitle = if (hasNote) "${category.displayName} · $dateText" else dateText,
                            amountText = CurrencyFormatter.format(expense.amount, state.currencySymbol),
                            onClick = { onEditExpense(expense.id) },
                            onLongClick = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }
}
