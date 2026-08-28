package com.nanokernel.expensetracker.ui.borrow

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
import com.nanokernel.expensetracker.data.local.BorrowEntity
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowListScreen(onBack: () -> Unit, onAddBorrow: () -> Unit, onEditBorrow: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: BorrowListViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BorrowListViewModel(app.borrowRepository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var borrowToDelete by remember { mutableStateOf<BorrowEntity?>(null) }

    borrowToDelete?.let { borrow ->
        AlertDialog(
            onDismissRequest = { borrowToDelete = null },
            title = { Text("Delete borrow entry?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteBorrow(borrow); borrowToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { borrowToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Borrowed", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onAddBorrow) {
                        Icon(Icons.Filled.Add, contentDescription = "Add borrow")
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
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total owed",
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
                Text("No borrows recorded this month.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn {
                    items(state.entries, key = { it.id }) { borrow ->
                        ExpenseListRow(
                            emoji = "💰",
                            color = MaterialTheme.colorScheme.error,
                            title = borrow.source?.takeIf { it.isNotBlank() } ?: "Borrowed",
                            subtitle = borrow.note?.takeIf { it.isNotBlank() } ?: DateUtils.formatDay(borrow.timestampMillis),
                            amountText = CurrencyFormatter.format(borrow.amount, state.currencySymbol),
                            onClick = { onEditBorrow(borrow.id) },
                            onLongClick = { borrowToDelete = borrow }
                        )
                    }
                }
            }
        }
    }
}
