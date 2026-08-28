package com.nanokernel.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowDao {

    @Insert
    suspend fun insert(borrow: BorrowEntity): Long

    @Update
    suspend fun update(borrow: BorrowEntity)

    @Delete
    suspend fun delete(borrow: BorrowEntity)

    @Query("SELECT * FROM borrows ORDER BY timestampMillis DESC")
    fun getAllBorrows(): Flow<List<BorrowEntity>>
}
