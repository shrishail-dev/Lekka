package com.nanokernel.expensetracker.ui.addexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.data.model.CategoryType
import com.nanokernel.expensetracker.ui.components.AddCategoryDialog
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.ui.components.CategoryChip
import com.nanokernel.expensetracker.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(onDone: () -> Unit, initialDateMillis: Long? = null, editingExpenseId: Long? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: AddExpenseViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AddExpenseViewModel(app.repository, app.settingsRepository, initialDateMillis, editingExpenseId)
            }
        }
    )
    val saved by viewModel.saved.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Editing loads an existing amount asynchronously — auto-focusing would just get in the way.
    LaunchedEffect(Unit) { if (!viewModel.isEditing) focusRequester.requestFocus() }
    LaunchedEffect(saved) { if (saved) onDone() }

    if (showDatePicker) {
        ExpenseDatePickerDialog(
            initialDate = DateUtils.toLocalDate(viewModel.dateMillis),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.onDateSelected(date)
                showDatePicker = false
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, emoji ->
                viewModel.addCategory(name, emoji)
                showAddCategoryDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 22.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (viewModel.isEditing) "Edit Expense" else "Add Expense",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                // Shrinks available height as the keyboard opens, so this screen's own scroll
                // (not a separate weighted section) carries the Save button into view instead
                // of leaving it hidden behind the keyboard.
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Amount first, with the numeric keypad focused on entry — the single most
                // important field for a "save in under 10 seconds" flow. A compact custom field
                // instead of OutlinedTextField, whose built-in label + padding make it much
                // taller than this needs to be.
                CompactAmountField(
                    value = viewModel.amountText,
                    onValueChange = viewModel::onAmountChange,
                    focusRequester = focusRequester,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = DateUtils.formatFullDate(DateUtils.toLocalDate(viewModel.dateMillis)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    leadingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change", style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                Spacer(Modifier.height(16.dp))
                Text("Category", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        CategoryChip(
                            category = category,
                            selected = viewModel.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelected(category.id) }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showAddCategoryDialog = true },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.height(14.dp)) },
                        label = { Text("New category", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text("Need or Want?", style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = viewModel.selectedType == CategoryType.NEED,
                        onClick = { viewModel.onTypeSelected(CategoryType.NEED) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Need", style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = viewModel.selectedType == CategoryType.WANT,
                        onClick = { viewModel.onTypeSelected(CategoryType.WANT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Want", style = MaterialTheme.typography.labelMedium) }
                }

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = viewModel.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text("Note (optional)", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = viewModel::save,
                enabled = viewModel.canSave,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    if (viewModel.isEditing) "Update Expense" else "Save Expense",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * A compact replacement for OutlinedTextField for the amount — that component's built-in
 * floating label and internal padding make it noticeably taller than this single most-used
 * field needs to be.
 */
@Composable
private fun CompactAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Amount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("₹", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            "0",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
    initialDate: java.time.LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (java.time.LocalDate) -> Unit
) {
    val today = DateUtils.toUtcDateMillis(java.time.LocalDate.now())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.toUtcDateMillis(initialDate),
        selectableDates = object : SelectableDates {
            // Expenses can be backdated but not logged for the future.
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= today
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onConfirm(DateUtils.fromUtcDateMillis(it)) }
                    ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

