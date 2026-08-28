package com.nanokernel.expensetracker.ui.calendar

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.ui.components.DeleteExpenseDialog
import com.nanokernel.expensetracker.ui.components.ExpenseListRow
import com.nanokernel.expensetracker.ui.components.ScreenHeader
import com.nanokernel.expensetracker.ui.theme.colorFor
import com.nanokernel.expensetracker.util.CsvExporter
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils
import com.nanokernel.expensetracker.util.PdfExporter
import com.nanokernel.expensetracker.util.notifyExportComplete
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private enum class ExportFormat { PDF, CSV }

@Composable
fun CalendarScreen(onExpenseClick: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: CalendarViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CalendarViewModel(app.repository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showFormatDialog by remember { mutableStateOf(false) }

    // Below Android 10 writing to the public Downloads folder needs the runtime permission;
    // on 10+ MediaStore handles it without one, so the launcher is only ever triggered pre-10.
    val latestState = rememberUpdatedState(state)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* if denied, notifyExportComplete's notify() call below just becomes a no-op */ }

    fun exportMonth(format: ExportFormat) {
        val current = latestState.value
        val result = when (format) {
            ExportFormat.CSV -> CsvExporter.exportMonthlyExpenses(
                context = context,
                month = current.month,
                expenses = current.monthExpenses,
                categories = current.categories
            )
            ExportFormat.PDF -> PdfExporter.exportMonthlyExpenses(
                context = context,
                month = current.month,
                expenses = current.monthExpenses,
                categories = current.categories,
                currencySymbol = current.currencySymbol
            )
        }
        if (result != null) {
            Toast.makeText(context, "Saved ${result.fileName} to Downloads/Lekka", Toast.LENGTH_LONG).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notifyExportComplete(context, result)
            }
        } else {
            Toast.makeText(context, "Couldn't save the export", Toast.LENGTH_LONG).show()
        }
    }
    var pendingFormat by remember { mutableStateOf<ExportFormat?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val format = pendingFormat
        pendingFormat = null
        if (granted && format != null) exportMonth(format)
        else if (!granted) Toast.makeText(context, "Storage permission needed to export", Toast.LENGTH_SHORT).show()
    }
    val onFormatSelected: (ExportFormat) -> Unit = { format ->
        showFormatDialog = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            pendingFormat = format
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            exportMonth(format)
        }
    }

    if (showFormatDialog) {
        ExportFormatDialog(
            onDismiss = { showFormatDialog = false },
            onFormatSelected = onFormatSelected
        )
    }

    expenseToDelete?.let { expense ->
        DeleteExpenseDialog(
            onDismiss = { expenseToDelete = null },
            onConfirm = { viewModel.deleteExpense(expense); expenseToDelete = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        ScreenHeader("Calendar")
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(DateUtils.formatMonthLabel(state.month), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showFormatDialog = true }) {
                    Icon(Icons.Filled.FileDownload, contentDescription = "Download this month's expenses")
                }
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        CalendarGrid(
            month = state.month,
            selectedDate = state.selectedDate,
            dayTotals = state.dayTotals,
            currencySymbol = state.currencySymbol,
            onDateClick = viewModel::selectDate,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        val selectedDate = state.selectedDate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                if (selectedDate != null) DateUtils.formatFullDate(selectedDate) else "All of ${DateUtils.formatMonthLabel(state.month)}",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                CurrencyFormatter.format(state.listTotal, state.currencySymbol),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))

        if (state.listExpenses.isEmpty()) {
            Text(
                if (selectedDate != null) "No expenses on this day." else "No expenses this month.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(state.listExpenses, key = { it.id }) { expense ->
                    DayExpenseRow(
                        expense = expense,
                        categories = state.categories,
                        currencySymbol = state.currencySymbol,
                        onClick = { onExpenseClick(expense.id) },
                        onLongClick = { expenseToDelete = expense }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    dayTotals: Map<LocalDate, Double>,
    currencySymbol: String,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value // 0..6
    val daysInMonth = month.lengthOfMonth()
    val totalCells = leadingBlanks + daysInMonth
    val rows = (totalCells + 6) / 7
    val today = LocalDate.now()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = month.atDay(dayNumber)
                            DayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                total = dayTotals[date] ?: 0.0,
                                currencySymbol = currencySymbol,
                                onClick = { onDateClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    total: Double,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall, color = textColor)
        if (total > 0) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .padding(top = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) textColor else MaterialTheme.colorScheme.tertiary)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun ExportFormatDialog(onDismiss: () -> Unit, onFormatSelected: (ExportFormat) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download expenses") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("PDF") },
                    supportingContent = { Text("Readable report, opens on any phone") },
                    leadingContent = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                    modifier = Modifier.clickable { onFormatSelected(ExportFormat.PDF) }
                )
                ListItem(
                    headlineContent = { Text("CSV") },
                    supportingContent = { Text("Raw data, opens in a spreadsheet app") },
                    leadingContent = { Icon(Icons.Filled.TableChart, contentDescription = null) },
                    modifier = Modifier.clickable { onFormatSelected(ExportFormat.CSV) }
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DayExpenseRow(
    expense: ExpenseEntity,
    categories: List<CategoryInfo>,
    currencySymbol: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val category = categories.findById(expense.category)
    ExpenseListRow(
        emoji = category.emoji,
        color = categories.colorFor(category.id),
        title = expense.note?.takeIf { it.isNotBlank() } ?: category.displayName,
        subtitle = category.displayName,
        amountText = CurrencyFormatter.format(expense.amount, currencySymbol),
        onClick = onClick,
        onLongClick = onLongClick
    )
}
