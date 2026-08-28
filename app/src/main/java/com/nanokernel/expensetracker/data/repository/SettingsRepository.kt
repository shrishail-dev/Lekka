package com.nanokernel.expensetracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Small app-wide settings: monthly budget, currency symbol, and user-created categories.
 * Backed by DataStore so changes survive process death and are observed reactively as Flows.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val BUDGET = doublePreferencesKey("monthly_budget")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CUSTOM_CATEGORIES = stringPreferencesKey("custom_categories")
    }

    companion object {
        const val DEFAULT_BUDGET = 15000.0
        const val DEFAULT_CURRENCY_SYMBOL = "₹"

        // Control characters as delimiters: never typed by a user, so no escaping needed.
        private const val FIELD_SEP = ""
        private const val RECORD_SEP = ""
    }

    val budgetFlow: Flow<Double> =
        context.dataStore.data.map { it[Keys.BUDGET] ?: DEFAULT_BUDGET }

    val currencySymbolFlow: Flow<String> =
        context.dataStore.data.map { it[Keys.CURRENCY_SYMBOL] ?: DEFAULT_CURRENCY_SYMBOL }

    private val customCategoriesFlow: Flow<List<CategoryInfo>> =
        context.dataStore.data.map { prefs ->
            val stored = prefs[Keys.CUSTOM_CATEGORIES].orEmpty()
            if (stored.isBlank()) emptyList()
            else stored.split(RECORD_SEP).mapNotNull { record ->
                // Accept 3 fields (current format) or the older 4-field format (id/name/emoji/type)
                // so categories created before Need/Want moved off the category still parse.
                val parts = record.split(FIELD_SEP)
                if (parts.size < 3) return@mapNotNull null
                CategoryInfo(id = parts[0], displayName = parts[1], emoji = parts[2], isCustom = true)
            }
        }

    /** Defaults + user-created categories. */
    val allCategoriesFlow: Flow<List<CategoryInfo>> =
        customCategoriesFlow.map { custom -> DefaultCategories.list + custom }

    suspend fun setBudget(value: Double) {
        context.dataStore.edit { it[Keys.BUDGET] = value }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    /** Creates a new category with a unique id derived from [displayName] and returns it. */
    suspend fun addCustomCategory(displayName: String, emoji: String): CategoryInfo {
        val current = customCategoriesFlow.first()
        val cleaned = displayName.trim().uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_')
        val baseId = "CUSTOM_" + cleaned.ifBlank { "CATEGORY" }
        val existingIds = (DefaultCategories.list.map { it.id } + current.map { it.id }).toSet()
        var uniqueId = baseId
        var suffix = 1
        while (uniqueId in existingIds) {
            uniqueId = "${baseId}_$suffix"
            suffix++
        }

        val newCategory = CategoryInfo(
            id = uniqueId,
            displayName = displayName.trim(),
            emoji = emoji.trim().ifBlank { "🏷️" },
            isCustom = true
        )
        persistCustomCategories(current + newCategory)
        return newCategory
    }

    private suspend fun persistCustomCategories(list: List<CategoryInfo>) {
        val serialized = list.joinToString(RECORD_SEP) { c ->
            listOf(c.id, c.displayName, c.emoji).joinToString(FIELD_SEP)
        }
        context.dataStore.edit { it[Keys.CUSTOM_CATEGORIES] = serialized }
    }
}
