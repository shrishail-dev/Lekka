package com.nanokernel.expensetracker.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.EventEntity
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EventSummary(val event: EventEntity, val total: Double, val expenseCount: Int)

data class EventListUiState(
    val activeEvents: List<EventSummary> = emptyList(),
    val archivedEvents: List<EventSummary> = emptyList(),
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class EventListViewModel(
    private val eventRepository: EventRepository,
    private val eventExpenseRepository: EventExpenseRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<EventListUiState> = combine(
        eventRepository.allEvents,
        eventExpenseRepository.allEventExpenses,
        settingsRepository.currencySymbolFlow
    ) { events, expenses, symbol ->
        val summaries = events.map { event ->
            val eventExpenses = expenses.filter { it.eventId == event.id }
            EventSummary(event, eventExpenses.sumOf { it.amount }, eventExpenses.size)
        }.sortedByDescending { it.event.createdDateMillis }

        EventListUiState(
            activeEvents = summaries.filter { !it.event.isArchived },
            archivedEvents = summaries.filter { it.event.isArchived },
            currencySymbol = symbol
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventListUiState())

    fun setArchived(event: EventEntity, archived: Boolean) {
        viewModelScope.launch { eventRepository.setArchived(event, archived) }
    }

    /** Room has no cascade set up between events and event_expenses, so its expenses are
     *  removed explicitly here first to avoid leaving orphan rows behind. */
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            eventExpenseRepository.allEventExpenses.first()
                .filter { it.eventId == event.id }
                .forEach { eventExpenseRepository.deleteExpense(it) }
            eventRepository.deleteEvent(event)
        }
    }
}
