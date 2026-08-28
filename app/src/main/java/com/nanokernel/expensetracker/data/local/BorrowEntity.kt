package com.nanokernel.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single borrow record — money currently owed. Multiple entries sum to the "Borrowed" total
 * shown on Home, mirroring how [ExpenseEntity] rows sum to the month total.
 */
@Entity(tableName = "borrows")
data class BorrowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val source: String?,
    val note: String?,
    val timestampMillis: Long
)
