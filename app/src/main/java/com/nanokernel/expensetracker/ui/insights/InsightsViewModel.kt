package com.nanokernel.expensetracker.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.local.categoryType
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.CategoryType
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.model.findById
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.CurrencyFormatter
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import kotlin.math.roundToInt

data class InsightItem(val emoji: String, val text: String)

data class InsightsUiState(
    val needsPercent: Int = 0,
    val wantsPercent: Int = 0,
    val insights: List<InsightItem> = emptyList(),
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class InsightsViewModel(
    private val repository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.allExpenses,
        settingsRepository.budgetFlow,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { expenses, budget, categories, symbol ->
        buildState(expenses, budget, categories, symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())

    private fun buildState(
        expenses: List<ExpenseEntity>,
        budget: Double,
        categories: List<CategoryInfo>,
        symbol: String
    ): InsightsUiState {
        val thisMonth = YearMonth.now()
        val lastMonth = thisMonth.minusMonths(1)
        val (thisStart, thisEnd) = DateUtils.monthRange(thisMonth)
        val (lastStart, lastEnd) = DateUtils.monthRange(lastMonth)

        val thisMonthExpenses = expenses.filter { it.timestampMillis in thisStart..thisEnd }
        val lastMonthExpenses = expenses.filter { it.timestampMillis in lastStart..lastEnd }
        val monthTotal = thisMonthExpenses.sumOf { it.amount }

        // Needs vs Wants split, driven by each expense's own tag — a category (e.g. Food) can
        // hold both, so this is more accurate than falling back to the category's type.
        val needsTotal = thisMonthExpenses
            .filter { it.categoryType() == CategoryType.NEED }
            .sumOf { it.amount }
        val needsPercent = if (monthTotal > 0) ((needsTotal / monthTotal) * 100).roundToInt() else 0
        val wantsPercent = if (monthTotal > 0) 100 - needsPercent else 0

        val insights = mutableListOf<InsightItem>()

        val categoryTotals = categories.associateWith { cat ->
            thisMonthExpenses.filter { it.category == cat.id }.sumOf { it.amount }
        }

        // 1. Biggest single category share of this month's spend.
        val topCategory = categoryTotals.maxByOrNull { it.value }
        if (topCategory != null && monthTotal > 0 && topCategory.value > 0) {
            val pct = ((topCategory.value / monthTotal) * 100).roundToInt()
            if (pct >= 25) {
                insights.add(InsightItem("⚠️", "${topCategory.key.displayName} is $pct% of your spending"))
            }
        }

        // 2. Biggest month-over-month category increase (only compares categories with prior spend).
        val lastMonthCategoryTotals = categories.associateWith { cat ->
            lastMonthExpenses.filter { it.category == cat.id }.sumOf { it.amount }
        }
        val trend = categories.mapNotNull { cat ->
            val prev = lastMonthCategoryTotals[cat] ?: 0.0
            val current = categoryTotals[cat] ?: 0.0
            if (prev > 0) cat to ((current - prev) / prev) * 100 else null
        }.maxByOrNull { it.second }
        if (trend != null && trend.second >= 20) {
            insights.add(InsightItem("📈", "'${trend.first.displayName}' spending up ${trend.second.roundToInt()}% vs last month"))
        }

        // 3. Projected month-end spend from the daily average so far.
        val daysElapsed = DateUtils.daysElapsedInMonth(thisMonth)
        val daysInMonth = DateUtils.daysInMonth(thisMonth)
        if (monthTotal > 0) {
            val projected = (monthTotal / daysElapsed) * daysInMonth
            insights.add(InsightItem("🔮", "At this rate you'll spend ${CurrencyFormatter.format(projected, symbol)} this month"))
        }

        // 4. Flag any single expense over 15% of the monthly budget.
        if (budget > 0) {
            thisMonthExpenses
                .filter { it.amount > budget * 0.15 }
                .sortedByDescending { it.amount }
                .forEach { expense ->
                    val label = expense.note?.takeIf { it.isNotBlank() } ?: categories.findById(expense.category).displayName
                    insights.add(InsightItem("🚩", "$label (${CurrencyFormatter.format(expense.amount, symbol)}) is over 15% of your budget"))
                }
        }

        return InsightsUiState(
            needsPercent = needsPercent,
            wantsPercent = wantsPercent,
            insights = insights,
            categories = categories,
            currencySymbol = symbol
        )
    }
}
