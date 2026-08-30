package com.nanokernel.expensetracker.data.repository

import com.nanokernel.expensetracker.data.local.LentDao
import com.nanokernel.expensetracker.data.local.LentEntity
import kotlinx.coroutines.flow.Flow

class LentRepository(private val dao: LentDao) {

    val allLent: Flow<List<LentEntity>> = dao.getAllLent()

    suspend fun addLent(amount: Double, recipient: String?, note: String?, timestampMillis: Long) {
        dao.insert(
            LentEntity(
                amount = amount,
                recipient = recipient?.trim()?.takeIf { it.isNotEmpty() },
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun updateLent(
        original: LentEntity,
        amount: Double,
        recipient: String?,
        note: String?,
        timestampMillis: Long
    ) {
        dao.update(
            original.copy(
                amount = amount,
                recipient = recipient?.trim()?.takeIf { it.isNotEmpty() },
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun deleteLent(lent: LentEntity) = dao.delete(lent)
}
