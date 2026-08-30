package com.nanokernel.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single lent record — money currently owed to you by someone else. Mirrors [BorrowEntity]
 * (money you owe) but in the opposite direction; multiple entries sum to the "Lent" total shown
 * on Home.
 */
@Entity(tableName = "lent")
data class LentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val recipient: String?,
    val note: String?,
    val timestampMillis: Long
)
