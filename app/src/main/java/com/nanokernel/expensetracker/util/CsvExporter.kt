package com.nanokernel.expensetracker.util

import android.content.Context
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import java.time.YearMonth

object CsvExporter {

    fun exportMonthlyExpenses(
        context: Context,
        month: YearMonth,
        expenses: List<ExpenseEntity>,
        categories: List<CategoryInfo>
    ): ExportResult? {
        val (groups, grandTotal) = groupMonthlyExpenses(month, expenses, categories)
        val csv = buildCsv(month, groups, grandTotal)
        val fileName = "Lekka_${month.month.name.lowercase().replaceFirstChar { it.uppercase() }}_${month.year}.csv"
        return DownloadFileWriter.write(context, fileName, "text/csv") { it.write(csv.toByteArray()) }
    }

    private fun buildCsv(month: YearMonth, groups: List<CategoryGroup>, grandTotal: Double): String = buildString {
        appendLine("Lekka - ${DateUtils.formatMonthLabel(month)}")
        appendLine("Date,Category,Note,Type,Amount")

        groups.forEach { group ->
            group.expenses.forEach { expense ->
                val note = (expense.note ?: "").replace("\"", "'").replace(",", ";")
                appendLine(
                    "${DateUtils.formatDay(expense.timestampMillis)}," +
                        "${group.category.displayName}," +
                        "$note," +
                        "${expense.type}," +
                        expense.amount
                )
            }
            appendLine(",,,${group.category.displayName} total,${group.subtotal}")
            appendLine()
        }

        appendLine(",,,Grand total,$grandTotal")
    }
}
