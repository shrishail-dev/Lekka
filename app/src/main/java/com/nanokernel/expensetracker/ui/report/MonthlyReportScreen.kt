package com.nanokernel.expensetracker.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.ui.components.BarEntry
import com.nanokernel.expensetracker.ui.components.DeleteExpenseDialog
import com.nanokernel.expensetracker.ui.components.DonutChart
import com.nanokernel.expensetracker.ui.components.DonutLegend
import com.nanokernel.expensetracker.ui.components.DonutSlice
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.ui.components.ScreenHeader
import com.nanokernel.expensetracker.ui.components.SimpleBarChart
import com.nanokernel.expensetracker.ui.theme.ReportAccent
import com.nanokernel.expensetracker.ui.theme.colorFor
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils

@Composable
fun MonthlyReportScreen(onExpenseClick: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: MonthlyReportViewModel = viewModel(
        factory = viewModelFactory {
            initializer { MonthlyReportViewModel(app.repository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            onDismiss = { expenseToDelete = null },
            onConfirm = { viewModel.deleteExpense(expense); expenseToDelete = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ScreenHeader("Report") }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(DateUtils.formatMonthLabel(state.month), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
        }

        if (state.categoryBreakdown.isEmpty()) {
            item {
                Text(
                    CurrencyFormatter.format(state.monthTotal, state.currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ReportAccent
                )
            }
            item { Text("No expenses recorded for this month.") }
        } else {
            item {
                val slices = state.categoryBreakdown.map { c ->
                    DonutSlice(c.category.displayName, c.category.emoji, c.amount, state.categories.colorFor(c.category.id), c.percent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    DonutChart(slices = slices, modifier = Modifier.size(200.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                CurrencyFormatter.format(state.monthTotal, state.currencySymbol),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ReportAccent
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    DonutLegend(slices = slices, currencySymbol = state.currencySymbol, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            Text("Last 3 Months", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "Total spend per month, so you can see if it's trending up or down.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            SimpleBarChart(
                entries = state.lastThreeMonths.map { BarEntry(it.label, it.amount, it.isCurrent) },
                currencySymbol = state.currencySymbol,
                barColor = ReportAccent,
                highlightColor = ReportAccent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        item {
            Text("Top 5 Expenses", style = MaterialTheme.typography.titleMedium)
        }

        if (state.topExpenses.isEmpty()) {
            item { Text("Nothing here yet.") }
        } else {
            item {
                Column {
                    state.topExpenses.forEach { expense ->
                        val category = state.categories.findById(expense.category)
                        ExpenseListRow(
                            emoji = category.emoji,
                            color = state.categories.colorFor(category.id),
                            title = expense.note?.takeIf { it.isNotBlank() } ?: category.displayName,
                            subtitle = category.displayName,
                            amountText = CurrencyFormatter.format(expense.amount, state.currencySymbol),
                            onClick = { onExpenseClick(expense.id) },
                            onLongClick = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }
}
