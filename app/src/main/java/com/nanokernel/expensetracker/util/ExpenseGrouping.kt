package com.nanokernel.expensetracker.util

import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.findById
import java.time.YearMonth

data class CategoryGroup(val category: CategoryInfo, val expenses: List<ExpenseEntity>, val subtotal: Double)

/** Shared by the CSV and PDF exporters so both report the same grouping/ordering/totals. */
fun groupMonthlyExpenses(
    month: YearMonth,
    expenses: List<ExpenseEntity>,
    categories: List<CategoryInfo>
): Pair<List<CategoryGroup>, Double> {
    val (monthStart, monthEnd) = DateUtils.monthRange(month)
    val monthExpenses = expenses.filter { it.timestampMillis in monthStart..monthEnd }

    val groups = monthExpenses
        .sortedBy { it.timestampMillis }
        .groupBy { categories.findById(it.category) }
        .map { (category, catExpenses) -> CategoryGroup(category, catExpenses, catExpenses.sumOf { it.amount }) }
        .sortedByDescending { it.subtotal }

    return groups to groups.sumOf { it.subtotal }
}
