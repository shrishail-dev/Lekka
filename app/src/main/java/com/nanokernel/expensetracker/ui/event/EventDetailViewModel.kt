package com.nanokernel.expensetracker.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.EventEntity
import com.nanokernel.expensetracker.data.local.EventExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: EventEntity? = null,
    val expenses: List<EventExpenseEntity> = emptyList(),
    val total: Double = 0.0,
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class EventDetailViewModel(
    private val eventId: Long,
    eventRepository: EventRepository,
    private val eventExpenseRepository: EventExpenseRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<EventDetailUiState> = combine(
        eventRepository.allEvents,
        eventExpenseRepository.allEventExpenses,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { events, allExpenses, categories, symbol ->
        val expenses = allExpenses.filter { it.eventId == eventId }
        EventDetailUiState(
            event = events.find { it.id == eventId },
            expenses = expenses,
            total = expenses.sumOf { it.amount },
            categories = categories,
            currencySymbol = symbol
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventDetailUiState())

    fun deleteExpense(expense: EventExpenseEntity) {
        viewModelScope.launch { eventExpenseRepository.deleteExpense(expense) }
    }
}
