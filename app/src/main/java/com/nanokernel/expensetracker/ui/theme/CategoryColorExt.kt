package com.nanokernel.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.nanokernel.expensetracker.data.model.CategoryInfo

/** A category's fixed chart color, keyed by its position in the full category list so it
 *  stays consistent across the donut, legend, and every list that shows this category. */
fun List<CategoryInfo>.colorFor(categoryId: String): Color {
    val index = indexOfFirst { it.id == categoryId }.coerceAtLeast(0)
    return CategoryColors[index % CategoryColors.size]
}
