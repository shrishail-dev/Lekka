package com.nanokernel.expensetracker.ui.lent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.LentEntity
import com.nanokernel.expensetracker.data.repository.LentRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddLentViewModel(
    private val repository: LentRepository,
    private val editingLentId: Long? = null
) : ViewModel() {

    val isEditing: Boolean get() = editingLentId != null

    var amountText by mutableStateOf("")
        private set
    var recipient by mutableStateOf("")
        private set
    var note by mutableStateOf("")
        private set
    var dateMillis by mutableStateOf(DateUtils.nowMillis())
        private set

    private var originalLent: LentEntity? = null

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    init {
        if (editingLentId != null) {
            viewModelScope.launch {
                val lent = repository.allLent.first().find { it.id == editingLentId } ?: return@launch
                originalLent = lent
                amountText = if (lent.amount == lent.amount.toLong().toDouble()) {
                    lent.amount.toLong().toString()
                } else {
                    lent.amount.toString()
                }
                recipient = lent.recipient.orEmpty()
                note = lent.note.orEmpty()
                dateMillis = lent.timestampMillis
            }
        }
    }

    val amountValue: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = amountValue > 0.0

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        amountText = if (filtered.count { it == '.' } > 1) amountText else filtered
    }

    fun onRecipientChange(value: String) {
        recipient = value
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
            val original = originalLent
            if (original != null) {
                repository.updateLent(original, amountValue, recipient, note, dateMillis)
            } else {
                repository.addLent(amountValue, recipient, note, dateMillis)
            }
            _saved.value = true
        }
    }
}
