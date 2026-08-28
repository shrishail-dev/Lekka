package com.nanokernel.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExpenseEntity::class, BorrowEntity::class, EventEntity::class, EventExpenseEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun borrowDao(): BorrowDao
    abstract fun eventDao(): EventDao
    abstract fun eventExpenseDao(): EventExpenseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db"
                )
                    // No migration path defined yet for the v1 -> v2 (added `type`), v2 -> v3
                    // (added `borrows` table), or v3 -> v4 (added `events`/`event_expenses`)
                    // schema changes; acceptable pre-release, but revisit with a real Migration
                    // before shipping.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
