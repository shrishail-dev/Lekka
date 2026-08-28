package com.nanokernel.expensetracker.data.repository

import com.nanokernel.expensetracker.data.local.ExpenseDao
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryType
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {

    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()

    suspend fun addExpense(amount: Double, categoryId: String, type: CategoryType, note: String?, timestampMillis: Long) {
        dao.insert(
            ExpenseEntity(
                amount = amount,
                category = categoryId,
                type = type.name,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun updateExpense(
        original: ExpenseEntity,
        amount: Double,
        categoryId: String,
        type: CategoryType,
        note: String?,
        timestampMillis: Long
    ) {
        dao.update(
            original.copy(
                amount = amount,
                category = categoryId,
                type = type.name,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = dao.delete(expense)
}
