package com.nanokernel.expensetracker

import android.app.Application
import android.util.Log
import com.nanokernel.expensetracker.data.local.AppDatabase
import com.nanokernel.expensetracker.data.repository.BorrowRepository
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
import com.nanokernel.expensetracker.data.repository.LentRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.reminder.ReminderScheduler
import com.nanokernel.expensetracker.reminder.createReminderNotificationChannel
import com.nanokernel.expensetracker.util.createExportNotificationChannel

/**
 * Holds app-wide singletons (DB + repositories) so every screen's ViewModel factory
 * can reach the same instances without a DI framework.
 */
class ExpenseTrackerApp : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
    val borrowRepository by lazy { BorrowRepository(database.borrowDao()) }
    val eventRepository by lazy { EventRepository(database.eventDao()) }
    val eventExpenseRepository by lazy { EventExpenseRepository(database.eventExpenseDao()) }
    val lentRepository by lazy { LentRepository(database.lentDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Startup housekeeping (notification channels, arming the reminder alarm) must never be
        // able to crash the whole app before a single screen renders — if any OEM/Android
        // version quirk trips one of these, log it and keep going instead of taking Lekka down.
        runCatching { createExportNotificationChannel(this) }
            .onFailure { Log.e("ExpenseTrackerApp", "createExportNotificationChannel failed", it) }
        runCatching { createReminderNotificationChannel(this) }
            .onFailure { Log.e("ExpenseTrackerApp", "createReminderNotificationChannel failed", it) }
        runCatching { ReminderScheduler.scheduleDaily(this) }
            .onFailure { Log.e("ExpenseTrackerApp", "ReminderScheduler.scheduleDaily failed", it) }
    }
}
