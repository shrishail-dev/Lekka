package com.nanokernel.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A custom event (wedding, birthday, naming ceremony, ...) that groups its own spending
 * separately from the monthly budget — see [EventExpenseEntity]. [budget] is optional since not
 * every event needs one tracked.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val budget: Double?,
    val createdDateMillis: Long,
    val isArchived: Boolean = false
)
