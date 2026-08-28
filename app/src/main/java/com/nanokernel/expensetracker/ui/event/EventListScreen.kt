package com.nanokernel.expensetracker.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(onBack: () -> Unit, onAddEvent: () -> Unit, onEventClick: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: EventListViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EventListViewModel(app.eventRepository, app.eventExpenseRepository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var eventToDelete by remember { mutableStateOf<EventSummary?>(null) }

    eventToDelete?.let { summary ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete \"${summary.event.name}\"?") },
            text = { Text("This deletes the event and all ${summary.expenseCount} expense(s) logged under it. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(summary.event); eventToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { eventToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Events", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onAddEvent) {
                        Icon(Icons.Filled.Add, contentDescription = "New event")
                    }
                }
            )
        }
    ) { padding ->
        if (state.activeEvents.isEmpty() && state.archivedEvents.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Text(
                    "No events yet. Tap + to track spending for a wedding, birthday, or any one-off occasion — kept separate from your monthly budget.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.activeEvents.isNotEmpty()) {
                    items(state.activeEvents, key = { it.event.id }) { summary ->
                        EventCard(
                            summary = summary,
                            currencySymbol = state.currencySymbol,
                            onClick = { onEventClick(summary.event.id) },
                            onArchive = { viewModel.setArchived(summary.event, true) },
                            onDelete = { eventToDelete = summary }
                        )
                    }
                }
                if (state.archivedEvents.isNotEmpty()) {
                    item {
                        Text(
                            "ARCHIVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(state.archivedEvents, key = { it.event.id }) { summary ->
                        EventCard(
                            summary = summary,
                            currencySymbol = state.currencySymbol,
                            onClick = { onEventClick(summary.event.id) },
                            onArchive = { viewModel.setArchived(summary.event, false) },
                            onDelete = { eventToDelete = summary },
                            isArchived = true
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventCard(
    summary: EventSummary,
    currencySymbol: String,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    isArchived: Boolean = false
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onDelete),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isArchived) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(summary.event.emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.event.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                val budget = summary.event.budget
                Text(
                    if (budget != null) {
                        "${summary.expenseCount} expense(s) · budget ${CurrencyFormatter.format(budget, currencySymbol)}"
                    } else {
                        "${summary.expenseCount} expense(s)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(summary.total, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                // A plain clickable Text, not TextButton — TextButton's built-in content padding
                // would inset "Archive" from the column's right edge while the amount above sits
                // flush against it, misaligning the two lines.
                Text(
                    if (isArchived) "Unarchive" else "Archive",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onArchive)
                )
            }
        }
    }
}
