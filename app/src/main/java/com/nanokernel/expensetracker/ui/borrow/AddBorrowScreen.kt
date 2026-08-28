package com.nanokernel.expensetracker.ui.borrow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.ui.components.AppLogo
import com.nanokernel.expensetracker.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBorrowScreen(onDone: () -> Unit, editingBorrowId: Long? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: AddBorrowViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AddBorrowViewModel(app.borrowRepository, editingBorrowId) }
        }
    )
    val saved by viewModel.saved.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(saved) { if (saved) onDone() }

    if (showDatePicker) {
        BorrowDatePickerDialog(
            initialDate = DateUtils.toLocalDate(viewModel.dateMillis),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.onDateSelected(date)
                showDatePicker = false
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
                            if (viewModel.isEditing) "Edit Borrow" else "Add Borrow",
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.source,
                    onValueChange = viewModel::onSourceChange,
                    label = { Text("Borrowed from (optional)", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text("Note (optional)", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::save,
                enabled = viewModel.canSave,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    if (viewModel.isEditing) "Update Borrow" else "Save Borrow",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BorrowDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = DateUtils.toUtcDateMillis(LocalDate.now())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.toUtcDateMillis(initialDate),
        selectableDates = object : SelectableDates {
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
