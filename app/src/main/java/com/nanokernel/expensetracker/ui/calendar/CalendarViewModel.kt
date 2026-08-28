package com.nanokernel.expensetracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val dayTotals: Map<LocalDate, Double> = emptyMap(),
    val listExpenses: List<ExpenseEntity> = emptyList(),
    val listTotal: Double = 0.0,
    val monthExpenses: List<ExpenseEntity> = emptyList(),
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class CalendarViewModel(
    private val repository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.allExpenses,
        selectedMonth,
        selectedDate,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { expenses, month, date, categories, symbol ->
        buildState(expenses, month, date, categories, symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    // Changing month always drops back to the whole-month view — a date picked in a
    // different month has no meaning here anymore.
    fun previousMonth() = changeMonth(selectedMonth.value.minusMonths(1))
    fun nextMonth() = changeMonth(selectedMonth.value.plusMonths(1))

    private fun changeMonth(newMonth: YearMonth) {
        selectedMonth.value = newMonth
        selectedDate.value = null
    }

    /** Tapping the already-selected date deselects it, returning to the whole-month view. */
    fun selectDate(date: LocalDate) {
        selectedDate.value = if (selectedDate.value == date) null else date
        if (YearMonth.from(date) != selectedMonth.value) {
            selectedMonth.value = YearMonth.from(date)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    private fun buildState(
        expenses: List<ExpenseEntity>,
        month: YearMonth,
        date: LocalDate?,
        categories: List<CategoryInfo>,
        symbol: String
    ): CalendarUiState {
        val (monthStart, monthEnd) = DateUtils.monthRange(month)
        val monthExpenses = expenses.filter { it.timestampMillis in monthStart..monthEnd }
        val dayTotals = monthExpenses
            .groupBy { DateUtils.toLocalDate(it.timestampMillis) }
            .mapValues { (_, dayExpenses) -> dayExpenses.sumOf { it.amount } }

        val (listExpenses, listTotal) = if (date != null) {
            val (dayStart, dayEnd) = DateUtils.dayRange(date)
            val dayExpenses = expenses
                .filter { it.timestampMillis in dayStart..dayEnd }
                .sortedByDescending { it.timestampMillis }
            dayExpenses to dayExpenses.sumOf { it.amount }
        } else {
            monthExpenses.sortedByDescending { it.timestampMillis } to monthExpenses.sumOf { it.amount }
        }

        return CalendarUiState(
            month = month,
            selectedDate = date,
            dayTotals = dayTotals,
            listExpenses = listExpenses,
            listTotal = listTotal,
            monthExpenses = monthExpenses,
            categories = categories,
            currencySymbol = symbol
        )
    }
}
