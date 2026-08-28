package com.nanokernel.expensetracker.data.model

/**
 * Whether a single expense is essential spending or discretionary — chosen per expense (not
 * per category, since one category like Food can hold both a grocery run and a night out).
 * Drives the Insights Needs vs Wants split.
 */
enum class CategoryType { NEED, WANT }

/**
 * A spending category — either one of the built-in defaults or one the user created.
 * [id] is the stable key stored on every [com.nanokernel.expensetracker.data.local.ExpenseEntity];
 * it never changes even if [displayName] is edited later.
 */
data class CategoryInfo(
    val id: String,
    val displayName: String,
    val emoji: String,
    val isCustom: Boolean = false
) {
    val label: String get() = "$emoji $displayName"
}

object DefaultCategories {
    val list = listOf(
        CategoryInfo("FOOD", "Food", "🍔"),
        CategoryInfo("TRANSPORT", "Transport", "🚗"),
        CategoryInfo("GROCERIES", "Groceries", "🛒"),
        CategoryInfo("BILLS", "Bills", "💡"),
        CategoryInfo("FUN", "Fun", "🎬"),
        CategoryInfo("SHOPPING", "Shopping", "🛍️")
    )
}

/** Looks up a category by its stored id, falling back to the first known category if it's missing. */
fun List<CategoryInfo>.findById(id: String): CategoryInfo =
    find { it.id == id } ?: firstOrNull() ?: DefaultCategories.list.first()
