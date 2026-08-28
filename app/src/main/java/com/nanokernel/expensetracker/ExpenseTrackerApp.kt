package com.nanokernel.expensetracker

import android.app.Application
import com.nanokernel.expensetracker.data.local.AppDatabase
import com.nanokernel.expensetracker.data.repository.BorrowRepository
import com.nanokernel.expensetracker.data.repository.EventExpenseRepository
import com.nanokernel.expensetracker.data.repository.EventRepository
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
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
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createExportNotificationChannel(this)
        createReminderNotificationChannel(this)
        ReminderScheduler.scheduleDaily(this)
    }
}
