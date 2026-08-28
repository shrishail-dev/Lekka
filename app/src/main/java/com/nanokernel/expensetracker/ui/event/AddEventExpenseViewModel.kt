package com.nanokernel.expensetracker.ui.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.EventExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddEventExpenseViewModel(
    private val eventId: Long,
    private val repository: EventExpenseRepository,
    private val settingsRepository: SettingsRepository,
    private val editingExpenseId: Long? = null
) : ViewModel() {

    val isEditing: Boolean get() = editingExpenseId != null

    val categories: StateFlow<List<CategoryInfo>> = settingsRepository.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultCategories.list)

    var amountText by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf(DefaultCategories.list.first().id)
        private set
    var note by mutableStateOf("")
        private set
    var dateMillis by mutableStateOf(DateUtils.nowMillis())
        private set

    private var originalExpense: EventExpenseEntity? = null

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    init {
        if (editingExpenseId != null) {
            viewModelScope.launch {
                val expense = repository.allEventExpenses.first().find { it.id == editingExpenseId } ?: return@launch
                originalExpense = expense
                amountText = if (expense.amount == expense.amount.toLong().toDouble()) {
                    expense.amount.toLong().toString()
                } else {
                    expense.amount.toString()
                }
                selectedCategoryId = expense.category
                note = expense.note.orEmpty()
                dateMillis = expense.timestampMillis
            }
        }
    }

    val amountValue: Double get() = amountText.toDoubleOrNull() ?: 0.0
    val canSave: Boolean get() = amountValue > 0.0

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        amountText = if (filtered.count { it == '.' } > 1) amountText else filtered
    }

    fun onCategorySelected(categoryId: String) {
        selectedCategoryId = categoryId
    }

    fun onNoteChange(value: String) {
        note = value
    }

    fun onDateSelected(date: LocalDate) {
        dateMillis = DateUtils.withDate(dateMillis, date)
    }

    /** Creates a new user category and immediately selects it. */
    fun addCategory(displayName: String, emoji: String) {
        if (displayName.isBlank()) return
        viewModelScope.launch {
            val created = settingsRepository.addCustomCategory(displayName, emoji)
            selectedCategoryId = created.id
        }
    }

    fun save() {
        if (!canSave) return
        viewModelScope.launch {
            val original = originalExpense
            if (original != null) {
                repository.updateExpense(original, amountValue, selectedCategoryId, note, dateMillis)
            } else {
                repository.addExpense(eventId, amountValue, selectedCategoryId, note, dateMillis)
            }
            _saved.value = true
        }
    }
}
