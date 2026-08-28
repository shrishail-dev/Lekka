package com.nanokernel.expensetracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// SQL below is copied verbatim from app/schemas/.../4.json (Room's own compiled schema for the
// current entities) so each migration's resulting table matches exactly what Room expects —
// a mismatch here fails Room's post-migration schema validation. Whenever an entity changes,
// regenerate that file (`gradle kspDebugKotlin`, exportSchema = true in AppDatabase) and add a
// new Migration the same way — never fall back to fallbackToDestructiveMigration() again.

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 'WANT' matches ExpenseEntity.type's @ColumnInfo(defaultValue = "WANT") — existing rows
        // predate the Need/Want split, and Want is the more common default.
        db.execSQL("ALTER TABLE `expenses` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'WANT'")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `borrows` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`amount` REAL NOT NULL, `source` TEXT, `note` TEXT, `timestampMillis` INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `emoji` TEXT NOT NULL, `budget` REAL, " +
                "`createdDateMillis` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `event_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`eventId` INTEGER NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, " +
                "`note` TEXT, `timestampMillis` INTEGER NOT NULL)"
        )
    }
}

val allMigrations = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
