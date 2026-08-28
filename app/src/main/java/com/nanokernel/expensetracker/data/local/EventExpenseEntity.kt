package com.nanokernel.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single spend tied to an [EventEntity] via [eventId]. Kept in its own table — not
 * [ExpenseEntity] with a nullable event column — so event spending can never accidentally leak
 * into the monthly/daily totals every other screen computes from [ExpenseEntity].
 */
@Entity(tableName = "event_expenses")
data class EventExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val amount: Double,
    val category: String,
    val note: String?,
    val timestampMillis: Long
)
