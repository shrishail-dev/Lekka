package com.nanokernel.expensetracker.ui.report

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
import java.time.YearMonth

data class CategoryAmount(val category: CategoryInfo, val amount: Double, val percent: Int)
data class MonthAmount(val label: String, val amount: Double, val isCurrent: Boolean = false)

data class ReportUiState(
    val month: YearMonth = YearMonth.now(),
    val monthTotal: Double = 0.0,
    val categoryBreakdown: List<CategoryAmount> = emptyList(),
    val lastThreeMonths: List<MonthAmount> = emptyList(),
    val topExpenses: List<ExpenseEntity> = emptyList(),
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class MonthlyReportViewModel(
    private val repository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<ReportUiState> = combine(
        repository.allExpenses,
        selectedMonth,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { expenses, month, categories, symbol ->
        buildState(expenses, month, categories, symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

    fun previousMonth() { selectedMonth.value = selectedMonth.value.minusMonths(1) }
    fun nextMonth() { selectedMonth.value = selectedMonth.value.plusMonths(1) }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    private fun buildState(
        expenses: List<ExpenseEntity>,
        month: YearMonth,
        categories: List<CategoryInfo>,
        symbol: String
    ): ReportUiState {
        val (start, end) = DateUtils.monthRange(month)
        val monthExpenses = expenses.filter { it.timestampMillis in start..end }
        val monthTotal = monthExpenses.sumOf { it.amount }

        val breakdown = categories.mapNotNull { category ->
            val sum = monthExpenses.filter { it.category == category.id }.sumOf { it.amount }
            if (sum <= 0.0) null
            else CategoryAmount(
                category = category,
                amount = sum,
                percent = if (monthTotal > 0) ((sum / monthTotal) * 100).toInt() else 0
            )
        }.sortedByDescending { it.amount }

        // Always the selected month and the two before it, regardless of whether they have data.
        val lastThree = (2 downTo 0).map { offset ->
            val m = month.minusMonths(offset.toLong())
            val (s, e) = DateUtils.monthRange(m)
            val total = expenses.filter { it.timestampMillis in s..e }.sumOf { it.amount }
            MonthAmount(DateUtils.formatShortMonthLabel(m), total, isCurrent = m == month)
        }

        val top5 = monthExpenses.sortedByDescending { it.amount }.take(5)

        return ReportUiState(
            month = month,
            monthTotal = monthTotal,
            categoryBreakdown = breakdown,
            lastThreeMonths = lastThree,
            topExpenses = top5,
            categories = categories,
            currencySymbol = symbol
        )
    }
}
