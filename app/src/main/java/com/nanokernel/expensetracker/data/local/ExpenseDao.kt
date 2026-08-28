package com.nanokernel.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    // Single source of truth: every screen derives its numbers (totals, charts, insights)
    // from this one stream, so the UI updates instantly after an insert/delete with no
    // manual refresh calls anywhere.
    @Query("SELECT * FROM expenses ORDER BY timestampMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
}
