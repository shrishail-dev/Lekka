package com.nanokernel.expensetracker.data.repository

import com.nanokernel.expensetracker.data.local.BorrowDao
import com.nanokernel.expensetracker.data.local.BorrowEntity
import kotlinx.coroutines.flow.Flow

class BorrowRepository(private val dao: BorrowDao) {

    val allBorrows: Flow<List<BorrowEntity>> = dao.getAllBorrows()

    suspend fun addBorrow(amount: Double, source: String?, note: String?, timestampMillis: Long) {
        dao.insert(
            BorrowEntity(
                amount = amount,
                source = source?.trim()?.takeIf { it.isNotEmpty() },
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun updateBorrow(
        original: BorrowEntity,
        amount: Double,
        source: String?,
        note: String?,
        timestampMillis: Long
    ) {
        dao.update(
            original.copy(
                amount = amount,
                source = source?.trim()?.takeIf { it.isNotEmpty() },
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = timestampMillis
            )
        )
    }

    suspend fun deleteBorrow(borrow: BorrowEntity) = dao.delete(borrow)
}
