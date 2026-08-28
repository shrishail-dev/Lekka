package com.nanokernel.expensetracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

const val REMINDER_CHANNEL_ID = "daily_reminder"
const val REMINDER_HOUR = 20 // 8 PM local time — after most people's day of spending is done.
const val REMINDER_MINUTE = 0
private const val REMINDER_REQUEST_CODE = 1001

object ReminderScheduler {

    /** Idempotent — safe to call on every app start; re-arms the same alarm if one is already set. */
    fun scheduleDaily(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context)
        // setAndAllowWhileIdle (not exact) needs no special permission and is plenty precise for
        // a once-a-day nudge — a few minutes' Doze-deferral doesn't matter here.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis(), pendingIntent)
    }

    fun rescheduleNextDay(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis(minDelayMinutes = 60), pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Today at [REMINDER_HOUR]:[REMINDER_MINUTE] if that's still [minDelayMinutes] away, else tomorrow. */
    private fun nextTriggerMillis(minDelayMinutes: Int = 0): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val minTrigger = System.currentTimeMillis() + minDelayMinutes * 60_000L
        if (calendar.timeInMillis <= minTrigger) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
