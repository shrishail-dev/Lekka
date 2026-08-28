package com.nanokernel.expensetracker.ui.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.EventEntity
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEventViewModel(
    private val repository: EventRepository,
    private val editingEventId: Long? = null
) : ViewModel() {

    val isEditing: Boolean get() = editingEventId != null

    var name by mutableStateOf("")
        private set
    var emoji by mutableStateOf("🎉")
        private set
    var budgetText by mutableStateOf("")
        private set

    private var originalEvent: EventEntity? = null

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    init {
        if (editingEventId != null) {
            viewModelScope.launch {
                val event = repository.allEvents.first().find { it.id == editingEventId } ?: return@launch
                originalEvent = event
                name = event.name
                emoji = event.emoji
                budgetText = event.budget?.let {
                    if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                }.orEmpty()
            }
        }
    }

    val canSave: Boolean get() = name.isNotBlank()

    fun onNameChange(value: String) {
        name = value
    }

    fun onEmojiChange(value: String) {
        emoji = value
    }

    fun onBudgetChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        budgetText = if (filtered.count { it == '.' } > 1) budgetText else filtered
    }

    fun save() {
        if (!canSave) return
        viewModelScope.launch {
            val budget = budgetText.toDoubleOrNull()
            val original = originalEvent
            if (original != null) {
                repository.updateEvent(original, name, emoji, budget)
            } else {
                repository.addEvent(name, emoji, budget, DateUtils.nowMillis())
            }
            _saved.value = true
        }
    }
}
