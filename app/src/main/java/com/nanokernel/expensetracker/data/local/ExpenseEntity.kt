package com.nanokernel.expensetracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nanokernel.expensetracker.data.model.CategoryType

/**
 * A single expense record. [category] stores a [com.nanokernel.expensetracker.data.model.CategoryInfo]
 * id (not a foreign key — categories live in DataStore, not Room), [type] is a [CategoryType] name
 * tagged per-expense (a category can hold both Need and Want spending, e.g. Food), and
 * [timestampMillis] is epoch millis in the device's local time.
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    // Explicit default so the v1->v2 Migration's ALTER TABLE ... DEFAULT clause matches Room's
    // own expected schema for this column — otherwise migration validation fails on upgrade.
    @ColumnInfo(defaultValue = "WANT") val type: String = "WANT",
    val note: String?,
    val timestampMillis: Long
)

fun ExpenseEntity.categoryType(): CategoryType =
    runCatching { CategoryType.valueOf(type) }.getOrDefault(CategoryType.WANT)
