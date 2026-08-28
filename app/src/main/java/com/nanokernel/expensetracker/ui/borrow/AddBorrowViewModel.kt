package com.nanokernel.expensetracker.ui.borrow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.BorrowEntity
import com.nanokernel.expensetracker.data.repository.BorrowRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddBorrowViewModel(
    private val repository: BorrowRepository,
    private val editingBorrowId: Long? = null
) : ViewModel() {

    val isEditing: Boolean get() = editingBorrowId != null

    var amountText by mutableStateOf("")
        private set
    var source by mutableStateOf("")
        private set
    var note by mutableStateOf("")
        private set
    var dateMillis by mutableStateOf(DateUtils.nowMillis())
        private set

    private var originalBorrow: BorrowEntity? = null

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    init {
        if (editingBorrowId != null) {
            viewModelScope.launch {
                val borrow = repository.allBorrows.first().find { it.id == editingBorrowId } ?: return@launch
                originalBorrow = borrow
                amountText = if (borrow.amount == borrow.amount.toLong().toDouble()) {
                    borrow.amount.toLong().toString()
                } else {
                    borrow.amount.toString()
                }
                source = borrow.source.orEmpty()
                note = borrow.note.orEmpty()
                dateMillis = borrow.timestampMillis
            }
        }
    }

    val amountValue: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = amountValue > 0.0

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        amountText = if (filtered.count { it == '.' } > 1) amountText else filtered
    }

    fun onSourceChange(value: String) {
        source = value
    }

    fun onNoteChange(value: String) {
        note = value
    }

    fun onDateSelected(date: LocalDate) {
        dateMillis = DateUtils.withDate(dateMillis, date)
    }

    fun save() {
        if (!canSave) return
        viewModelScope.launch {
            val original = originalBorrow
            if (original != null) {
                repository.updateBorrow(original, amountValue, source, note, dateMillis)
            } else {
                repository.addBorrow(amountValue, source, note, dateMillis)
            }
            _saved.value = true
        }
    }
}
