package com.nanokernel.expensetracker.ui.lent

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.nanokernel.expensetracker.data.local.LentEntity
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LentListScreen(onBack: () -> Unit, onAddLent: () -> Unit, onEditLent: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: LentListViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LentListViewModel(app.lentRepository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var lentToDelete by remember { mutableStateOf<LentEntity?>(null) }

    lentToDelete?.let { lent ->
        AlertDialog(
            onDismissRequest = { lentToDelete = null },
            title = { Text("Delete lent entry?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLent(lent); lentToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { lentToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Lent", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onAddLent) {
                        Icon(Icons.Filled.Add, contentDescription = "Add lent entry")
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(DateUtils.formatMonthLabel(state.month), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("This month", style = MaterialTheme.typography.titleSmall)
                Text(
                    CurrencyFormatter.format(state.monthTotal, state.currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total owed to you",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    CurrencyFormatter.format(state.overallTotal, state.currencySymbol),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            if (state.entries.isEmpty()) {
                Text("No lent entries this month.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn {
                    items(state.entries, key = { it.id }) { lent ->
                        ExpenseListRow(
                            emoji = "🤝",
                            color = MaterialTheme.colorScheme.primary,
                            title = lent.recipient?.takeIf { it.isNotBlank() } ?: "Lent",
                            subtitle = lent.note?.takeIf { it.isNotBlank() } ?: DateUtils.formatDay(lent.timestampMillis),
                            amountText = CurrencyFormatter.format(lent.amount, state.currencySymbol),
                            onClick = { onEditLent(lent.id) },
                            onLongClick = { lentToDelete = lent }
                        )
                    }
                }
            }
        }
    }
}
