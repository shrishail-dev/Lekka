package com.nanokernel.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LentDao {

    @Insert
    suspend fun insert(lent: LentEntity): Long

    @Update
    suspend fun update(lent: LentEntity)

    @Delete
    suspend fun delete(lent: LentEntity)

    @Query("SELECT * FROM lent ORDER BY timestampMillis DESC")
    fun getAllLent(): Flow<List<LentEntity>>
}
