package com.nanokernel.expensetracker.util

import java.text.DecimalFormat

/** Formats amounts with a currency symbol. Swap the default symbol/pattern here to change currency app-wide. */
object CurrencyFormatter {
    private val wholeFormat = DecimalFormat("#,##,##0") // Indian-style digit grouping

    fun format(amount: Double, symbol: String = "₹"): String =
        "$symbol${wholeFormat.format(amount)}"

    fun formatWithDecimals(amount: Double, symbol: String = "₹"): String =
        "$symbol${DecimalFormat("#,##,##0.00").format(amount)}"

    /** Short form for tight spaces (chart labels): 1,20,000 -> ₹1.2L, 8,500 -> ₹8.5k. */
    fun formatCompact(amount: Double, symbol: String = "₹"): String = when {
        amount >= 1_00_000 -> "$symbol${DecimalFormat("#,##0.#").format(amount / 1_00_000)}L"
        amount >= 1_000 -> "$symbol${DecimalFormat("#,##0.#").format(amount / 1_000)}k"
        else -> "$symbol${wholeFormat.format(amount)}"
    }
}
