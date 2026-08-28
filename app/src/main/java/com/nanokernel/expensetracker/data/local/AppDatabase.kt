package com.nanokernel.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ExpenseEntity::class, BorrowEntity::class, EventEntity::class, EventExpenseEntity::class],
    version = 4,
    exportSchema = true
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
                    // Real migrations (see AppDatabaseMigrations.kt) — no fallback on purpose:
                    // a missing migration should crash loudly during development, not silently
                    // wipe a user's data the way fallbackToDestructiveMigration() would.
                    .addMigrations(*allMigrations)
                    .build().also { INSTANCE = it }
            }
    }
}
