package com.nanokernel.expensetracker.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

object DateUtils {
    private val zone = ZoneId.systemDefault()

    fun nowMillis(): Long = System.currentTimeMillis()

    /** Inclusive start/end epoch-millis bounds for the given calendar month. */
    fun monthRange(month: YearMonth): Pair<Long, Long> {
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun startOfToday(): Long = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()

    fun startOfWeek(): Long =
        LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant().toEpochMilli()

    /** Inclusive start/end epoch-millis bounds for a single calendar day. */
    fun dayRange(date: LocalDate): Pair<Long, Long> {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /** Swaps the calendar date on [originalMillis] to [newDate], keeping the original time-of-day. */
    fun withDate(originalMillis: Long, newDate: LocalDate): Long {
        val time = Instant.ofEpochMilli(originalMillis).atZone(zone).toLocalTime()
        return newDate.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }

    // Compose's DatePicker works in UTC-midnight epoch millis regardless of device timezone —
    // these two convert to/from that so the rest of the app can stay in local time.
    fun toUtcDateMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromUtcDateMillis(utcMillis: Long): LocalDate =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

    /** Midday on [date] in local time — used as a neutral timestamp when only a date is picked. */
    fun noonOf(date: LocalDate): Long = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    fun daysInMonth(month: YearMonth): Int = month.lengthOfMonth()

    /** Days elapsed so far in [month]; at least 1 so projections never divide by zero. */
    fun daysElapsedInMonth(month: YearMonth): Int {
        val today = YearMonth.now(zone)
        val elapsed = if (month == today) LocalDate.now(zone).dayOfMonth else month.lengthOfMonth()
        return elapsed.coerceAtLeast(1)
    }

    fun formatDay(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toString()

    fun formatMonthLabel(month: YearMonth): String =
        "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}"

    fun formatShortMonthLabel(month: YearMonth): String =
        month.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }

    fun formatFullDate(date: LocalDate): String {
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$monthName ${date.dayOfMonth}, ${date.year}"
    }
}
