package com.nanokernel.expensetracker.data.repository

import com.nanokernel.expensetracker.data.local.EventExpenseDao
import com.nanokernel.expensetracker.data.local.EventExpenseEntity
import kotlinx.coroutines.flow.Flow

class EventExpenseRepository(private val dao: EventExpenseDao) {

    val allEventExpenses: Flow<List<EventExpenseEntity>> = dao.getAllEventExpenses()

    suspend fun addExpense(eventId: Long, amount: Double, categoryId: String, note: String?, timestampMillis: Long) {
        dao.insert(
            EventExpenseEntity(
                eventId = eventId,
                amount = amount,
                category = categoryId,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun updateExpense(
        original: EventExpenseEntity,
        amount: Double,
        categoryId: String,
        note: String?,
        timestampMillis: Long
    ) {
        dao.update(
            original.copy(
                amount = amount,
                category = categoryId,
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun deleteExpense(expense: EventExpenseEntity) = dao.delete(expense)
}
