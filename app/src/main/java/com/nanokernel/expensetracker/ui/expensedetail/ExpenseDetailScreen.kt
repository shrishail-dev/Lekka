package com.nanokernel.expensetracker.ui.expensedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.data.local.categoryType
import com.nanokernel.expensetracker.data.model.CategoryType
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.ui.components.CategoryIcon
import com.nanokernel.expensetracker.ui.theme.colorFor
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(expenseId: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: ExpenseDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ExpenseDetailViewModel(app.repository, app.settingsRepository, expenseId) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    val expense = state.expense

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Expense", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (expense != null) {
                        IconButton(onClick = { onEdit(expenseId) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit expense")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (expense == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("This expense was deleted.", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        val category = state.categories.findById(expense.category)
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CategoryIcon(category.emoji, state.categories.colorFor(category.id), size = 64.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    CurrencyFormatter.format(expense.amount, state.currencySymbol),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(category.displayName, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            DetailRow("Date", DateUtils.formatFullDate(DateUtils.toLocalDate(expense.timestampMillis)))
            DetailRow("Category", category.label)
            DetailRow("Type", if (expense.categoryType() == CategoryType.NEED) "Need" else "Want")
            if (!expense.note.isNullOrBlank()) {
                DetailRow("Note", expense.note)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
