package com.nanokernel.expensetracker.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import java.time.YearMonth

private const val PAGE_WIDTH = 595 // A4 at 72dpi
private const val PAGE_HEIGHT = 842
private const val MARGIN = 36f
private const val ROW_HEIGHT = 18f

private const val COL_DATE = MARGIN
private const val COL_CATEGORY = MARGIN + 70f
private const val COL_NOTE = MARGIN + 190f
private const val COL_TYPE = MARGIN + 380f
private const val COL_AMOUNT = PAGE_WIDTH - MARGIN

object PdfExporter {

    fun exportMonthlyExpenses(
        context: Context,
        month: YearMonth,
        expenses: List<ExpenseEntity>,
        categories: List<CategoryInfo>,
        currencySymbol: String
    ): ExportResult? {
        val (groups, grandTotal) = groupMonthlyExpenses(month, expenses, categories)
        val document = PdfDocument()
        try {
            renderPages(document, month, groups, grandTotal, currencySymbol)
            val fileName = "Lekka_${month.month.name.lowercase().replaceFirstChar { it.uppercase() }}_${month.year}.pdf"
            return DownloadFileWriter.write(context, fileName, "application/pdf") { document.writeTo(it) }
        } finally {
            document.close()
        }
    }

    private fun renderPages(
        document: PdfDocument,
        month: YearMonth,
        groups: List<CategoryGroup>,
        grandTotal: Double,
        currencySymbol: String
    ) {
        val titlePaint = Paint().apply { textSize = 16f; typeface = Typeface.DEFAULT_BOLD }
        val headerPaint = Paint().apply { textSize = 10f; typeface = Typeface.DEFAULT_BOLD }
        val bodyPaint = Paint().apply { textSize = 10f }
        val boldPaint = Paint().apply { textSize = 10f; typeface = Typeface.DEFAULT_BOLD }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN + 8f

        canvas.drawText("Lekka - ${DateUtils.formatMonthLabel(month)}", MARGIN, y, titlePaint)
        y += 28f
        drawHeaderRow(canvas, y, headerPaint)
        y += ROW_HEIGHT

        fun newPageIfNeeded() {
            if (y > PAGE_HEIGHT - MARGIN - ROW_HEIGHT) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN + 8f
                drawHeaderRow(canvas, y, headerPaint)
                y += ROW_HEIGHT
            }
        }

        groups.forEach { group ->
            group.expenses.forEach { expense ->
                newPageIfNeeded()
                canvas.drawText(DateUtils.formatDay(expense.timestampMillis), COL_DATE, y, bodyPaint)
                canvas.drawText(group.category.displayName, COL_CATEGORY, y, bodyPaint)
                canvas.drawText(truncate(expense.note ?: "", 28), COL_NOTE, y, bodyPaint)
                canvas.drawText(expense.type, COL_TYPE, y, bodyPaint)
                canvas.drawText(
                    CurrencyFormatter.format(expense.amount, currencySymbol),
                    COL_AMOUNT,
                    y,
                    bodyPaint,
                    alignRight = true
                )
                y += ROW_HEIGHT
            }
            newPageIfNeeded()
            canvas.drawText("${group.category.displayName} total", COL_CATEGORY, y, boldPaint)
            canvas.drawText(
                CurrencyFormatter.format(group.subtotal, currencySymbol),
                COL_AMOUNT,
                y,
                boldPaint,
                alignRight = true
            )
            y += ROW_HEIGHT * 1.5f
        }

        newPageIfNeeded()
        canvas.drawText("Grand total", COL_CATEGORY, y, titlePaint)
        canvas.drawText(
            CurrencyFormatter.format(grandTotal, currencySymbol),
            COL_AMOUNT,
            y,
            titlePaint,
            alignRight = true
        )

        document.finishPage(page)
    }

    private fun drawHeaderRow(canvas: android.graphics.Canvas, y: Float, paint: Paint) {
        canvas.drawText("Date", COL_DATE, y, paint)
        canvas.drawText("Category", COL_CATEGORY, y, paint)
        canvas.drawText("Note", COL_NOTE, y, paint)
        canvas.drawText("Type", COL_TYPE, y, paint)
        canvas.drawText("Amount", COL_AMOUNT, y, paint, alignRight = true)
    }

    private fun android.graphics.Canvas.drawText(text: String, x: Float, y: Float, paint: Paint, alignRight: Boolean) {
        val originalAlign = paint.textAlign
        paint.textAlign = if (alignRight) Paint.Align.RIGHT else Paint.Align.LEFT
        drawText(text, x, y, paint)
        paint.textAlign = originalAlign
    }

    private fun truncate(text: String, maxChars: Int): String =
        if (text.length > maxChars) text.take(maxChars - 1) + "…" else text
}
