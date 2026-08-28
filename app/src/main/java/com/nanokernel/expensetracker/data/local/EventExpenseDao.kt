package com.nanokernel.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventExpenseDao {

    @Insert
    suspend fun insert(expense: EventExpenseEntity): Long

    @Update
    suspend fun update(expense: EventExpenseEntity)

    @Delete
    suspend fun delete(expense: EventExpenseEntity)

    // Fetched in full and filtered by eventId in-memory (same pattern the rest of the app uses
    // for expenses/borrows), so the event list can total every event from one flow.
    @Query("SELECT * FROM event_expenses ORDER BY timestampMillis DESC")
    fun getAllEventExpenses(): Flow<List<EventExpenseEntity>>
}
