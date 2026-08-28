package com.nanokernel.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.repository.BorrowRepository
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class HomeUiState(
    val monthTotal: Double = 0.0,
    val budget: Double = SettingsRepository.DEFAULT_BUDGET,
    val borrowed: Double = 0.0,
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL,
    val activeEventCount: Int = 0,
    val activeEventsTotal: Double = 0.0
) {
    val balance: Double get() = budget - monthTotal
}

class HomeViewModel(
    private val repository: ExpenseRepository,
    private val borrowRepository: BorrowRepository,
    private val eventRepository: EventRepository,
    private val eventExpenseRepository: EventExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Only the active (non-archived) events' running total — kept entirely separate from
    // monthTotal/budget/balance above, never folded into them.
    private val eventsSummaryFlow: Flow<Pair<Int, Double>> = combine(
        eventRepository.allEvents,
        eventExpenseRepository.allEventExpenses
    ) { events, expenses ->
        val activeIds = events.filter { !it.isArchived }.map { it.id }.toSet()
        activeIds.size to expenses.filter { it.eventId in activeIds }.sumOf { it.amount }
    }

    private val baseFlow: Flow<HomeUiState> = combine(
        repository.allExpenses,
        borrowRepository.allBorrows,
        settingsRepository.budgetFlow,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { expenses, borrows, budget, categories, symbol ->
        val (monthStart, monthEnd) = DateUtils.monthRange(YearMonth.now())
        val todayStart = DateUtils.startOfToday()
        val weekStart = DateUtils.startOfWeek()

        HomeUiState(
            monthTotal = expenses.filter { it.timestampMillis in monthStart..monthEnd }.sumOf { it.amount },
            budget = budget,
            borrowed = borrows.sumOf { it.amount },
            todayTotal = expenses.filter { it.timestampMillis >= todayStart }.sumOf { it.amount },
            weekTotal = expenses.filter { it.timestampMillis >= weekStart }.sumOf { it.amount },
            recentExpenses = expenses.take(10),
            categories = categories,
            currencySymbol = symbol
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(baseFlow, eventsSummaryFlow) { base, (activeCount, eventsTotal) ->
        base.copy(activeEventCount = activeCount, activeEventsTotal = eventsTotal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun setBudget(newBudget: Double) {
        viewModelScope.launch { settingsRepository.setBudget(newBudget) }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}
